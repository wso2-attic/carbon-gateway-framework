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
import org.wso2.carbon.messaging.DefaultCarbonMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A class to decouple the mediators from the TypeConverterRegistry and to handle
 * the conversions.
 */
public class ConversionManager {
    private static final Logger log = LoggerFactory.getLogger(ConversionManager.class);
    private static final int EOF = -1;

    private static ConversionManager instance = new ConversionManager();

    private ConversionManager() {}

    public static ConversionManager getInstance() {
        return instance;
    }

    public CarbonMessage convertTo(CarbonMessage cMsg, String sourceType, String targetType) {

        TypeConverter converter = ConfigRegistry.getInstance().getTypeConverterRegistry()
                .getTypeConverter(sourceType, targetType);
        DefaultCarbonMessage newCarbonMsg = new DefaultCarbonMessage();

        if (converter == null) {
            if (log.isDebugEnabled()) {
                log.debug("No type converter found for Source: " + sourceType + " Target : " + targetType);
            }
            return null; // TODO: Throw an exception instead of returning null
        }

        BlockingQueue<ByteBuffer> contentBuf = getMessageBody(cMsg);
        InputStream inputStream = new ByteBufferBackedInputStream(contentBuf);
        InputStream processedStream = null;

        try {
            processedStream = converter.convert(inputStream);
            ByteBuffer outputByteBuffer = ByteBuffer.wrap(toByteArray(processedStream));
            newCarbonMsg.setHeader("Content-Type", targetType);
            newCarbonMsg.addMessageBody(outputByteBuffer);
            newCarbonMsg.setEndOfMsgAdded(true);
        } catch (TypeConversionException e) {
            log.error("Error in converting message body from: " + sourceType + " to: " + targetType);
        } catch (IOException e) {
            log.error("Error " + e);
        } finally {
            //TODO: do we need to close the input stream here ?
        }

        return newCarbonMsg;
    }

    private byte[] toByteArray(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int n;
        while (EOF != (n = in.read(buffer))) {
            out.write(buffer, 0, n);
        }
    }

    private BlockingQueue<ByteBuffer> getMessageBody(CarbonMessage msg) {
        BlockingQueue<ByteBuffer> msgBody = new LinkedBlockingQueue<>(msg.getFullMessageBody());
        return msgBody;
    }
}
