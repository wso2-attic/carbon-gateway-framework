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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A class to decouple the mediators from the TypeConverterRegsitry and to handle
 * the conversions.
 */
public class ConversionManager {
    private static final Logger log = LoggerFactory.getLogger(ConversionManager.class);
    private static ConversionManager manager;

    public static synchronized ConversionManager getInstance() {
        if (manager == null) {
            manager = new ConversionManager();
        }

        return manager;
    }

    public InputStream convertTo(CarbonMessage cMsg, String sourceType, String targetType) {
        TypeConverter converter = ConfigRegistry.getInstance()
                .getTypeConverterRegistry().lookup(targetType, sourceType);

        if (converter == null) {
            return null;
        }

        //aggregation and creating inputStream
        BlockingQueue<ByteBuffer> contentBuf = aggregateContent(cMsg);
        InputStream inputStream = new ByteBufferBackedInputStream(contentBuf);
        InputStream processedStream = null;

        try {
            processedStream = converter.convert(inputStream);
        } catch (TypeConversionException e) {
            log.error("Error in converting from: " + sourceType + " to: " + targetType);
        } catch (IOException e) {
            log.error("Error " + e);
        }
        return processedStream;
    }

    private BlockingQueue<ByteBuffer> aggregateContent(CarbonMessage msg) {

        try {
            //Check whether the message is fully read
            while (!msg.isEndOfMsgAdded()) {
                Thread.sleep(10);
            }
            //Get a clone of content chunk queue from the pipe
            BlockingQueue<ByteBuffer> clonedContent = new LinkedBlockingQueue<>(msg.getFullMessageBody());
            return clonedContent;
        } catch (Exception e) {
            log.error("Error while cloning ", e);
        }
        return null;
    }
}
