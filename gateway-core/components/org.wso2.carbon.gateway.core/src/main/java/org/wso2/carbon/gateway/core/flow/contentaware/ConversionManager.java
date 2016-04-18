/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.wso2.carbon.gateway.core.flow.contentaware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.config.ConfigRegistry;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.TypeConverter;
import org.wso2.carbon.gateway.core.flow.contentaware.exceptions.TypeConversionException;
import org.wso2.carbon.messaging.CarbonMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A class to decouple the mediators from the TypeConverterRegistry and to handle
 * the conversions.
 */
public class ConversionManager {
    private static final Logger log = LoggerFactory.getLogger(ConversionManager.class);

    private static ConversionManager instance = new ConversionManager();

    public static ConversionManager getInstance() {
        return instance;
    }

    private ConversionManager() {
    }

    ;

    public CarbonMessage convertTo(CarbonMessage cMsg, String targetType) {

        String sourceType = cMsg.getHeader("Content-Type");
        StringBuffer buf = new StringBuffer();

        TypeConverter converter = ConfigRegistry.getInstance().getTypeConverterRegistry()
                .getTypeConverter(targetType, sourceType);

        if (converter == null) {
            if (log.isDebugEnabled()) {
                log.debug("No type converted found for Source: " + sourceType + " Target : " + targetType);
            }
            return null;
        }

        //Aggregation and creating inputStream
        BlockingQueue<ByteBuffer> contentBuf = aggregateContent(cMsg);
        InputStream inputStream = new ByteBufferBackedInputStream(contentBuf);
        InputStream processedStream = null;

        try {
            processedStream = converter.convert(inputStream);
        } catch (TypeConversionException e) {
            log.error("Error in converting from: " + sourceType + " to: " + targetType);
        } catch (IOException e) {
            log.error("Error " + e);
        } finally {
            //TODO: do we need to close the input stream here ?
        }
        Scanner s = new java.util.Scanner(processedStream).useDelimiter("\\A");
        while (s.hasNext()) {
            buf.append(s.next());
        }
        String outputString = buf.toString();
        log.info(sourceType + " to " + targetType + " type conversion\n" + outputString);
        ByteBuffer outputByteBuffer = ByteBuffer.wrap(String.valueOf(buf).getBytes());
        cMsg.setHeader("Content-Type", targetType);
        cMsg.addMessageBody(outputByteBuffer);
        return cMsg;

    }

    private BlockingQueue<ByteBuffer> aggregateContent(CarbonMessage msg) {

        try {
            //Get a clone of content chunk queue from the pipe
            BlockingQueue<ByteBuffer> clonedContent = new LinkedBlockingQueue<>(msg.getFullMessageBody());
            return clonedContent;
        } catch (Exception e) {
            log.error("Error while cloning ", e);
        }
        return null;
    }
}
