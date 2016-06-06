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

package org.wso2.carbon.gateway.message.readers.jsonreader;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.MessageDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A class which represents JSON Message and JSON message Accessor.
 */
public class CarbonJSONMessageImpl implements MessageDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonJSONMessageImpl.class);

    private InputStream inputStream;
    private OutputStream outputStream;
    private String contentType;
    private String charsetEncoding;

    private JsonPath jsonPath;

    public CarbonJSONMessageImpl(String contentType, InputStream inputStream, OutputStream outputStream) {
        this.contentType = contentType;
        this.inputStream = JSONUtil.toReadOnlyStream(inputStream);
        this.outputStream = outputStream;
    }

    @Override
    public String getStringValue(String jsonPath) {
        this.jsonPath = JsonPath.compile(jsonPath);
        try {
            Object result = this.jsonPath.read(inputStream);
            return (null == result ? "null" : result.toString());
        } catch (IOException e) {
            String msg = "Error occurred while reading InputStream for evaluate JSONPath "
                    + "please check message format is correct";
            LOGGER.error(msg, e);
        } catch (PathNotFoundException pathNotFoundException) {
            return null;
        }
        return null;
    }

    @Override
    public Object getValue(String jsonPath) {
        return null;
    }

    @Override
    public Object getDataObject() {
        try {
            inputStream.reset();
        } catch (IOException e) {
            LOGGER.error("Error occured while resetting input stream", e);
        }
        return inputStream;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public String getCharsetEncoding() {
        return charsetEncoding;
    }

    public void setCharsetEncoding(String charsetEncoding) {
        this.charsetEncoding = charsetEncoding;
    }

    @Override
    public void setContentType(String s) {
        this.contentType = s;
    }

    @Override
    public void serializeData() {
        try {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            LOGGER.error("Exception occurred while copying JSON stream to OutputStream", e);
        }
    }

}
