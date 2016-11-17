/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.message.readers.jsonreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.Constants;
import org.wso2.ballerina.core.flow.contentaware.MIMEType;
import org.wso2.ballerina.core.flow.contentaware.messagereaders.AbstractReader;
import org.wso2.ballerina.core.flow.contentaware.messagereaders.ReaderUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageDataSource;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A class that responsible for providing the JSON inputStream.
 */
public class JSONReader extends AbstractReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONReader.class);

    public JSONReader(String contentType) {
        super(contentType);
    }

    @Override
    public MessageDataSource makeMessageReadable(CarbonMessage carbonMessage) throws Exception {
        String charset = null;
        InputStream inputStream = carbonMessage.getInputStream();
        OutputStream outputStream = carbonMessage.getOutputStream();
        String contentType = carbonMessage.getHeader(Constants.HTTP_CONTENT_TYPE);
        CarbonJSONMessageImpl carbonJSONMessage;
        if (contentType == null) {
            contentType = MIMEType.APPLICATION_JSON;
        } else {
            contentType = ReaderUtil.parseContentType(contentType);
            charset = ReaderUtil.parseCharset(contentType);
        }
        try {
            carbonMessage.setProperty(Constants.CHARACTER_SET_ENCODING, charset);

            carbonJSONMessage = new CarbonJSONMessageImpl(contentType, inputStream, outputStream);
            carbonJSONMessage.setCharsetEncoding(charset);
            attachMessageDataSource(carbonJSONMessage, carbonMessage);

        } catch (Exception e) {
            String msg = "Error occurred while building message using JSONBuilder for content type " + contentType;
            LOGGER.error(msg, e);
            throw new Exception(msg, e);
        }
        return carbonJSONMessage;
    }

    @Override
    public String getContentType() {
        return MIMEType.APPLICATION_JSON;
    }
}
