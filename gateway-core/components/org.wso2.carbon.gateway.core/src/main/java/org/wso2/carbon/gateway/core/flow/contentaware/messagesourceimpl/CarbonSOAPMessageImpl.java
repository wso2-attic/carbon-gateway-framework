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

package org.wso2.carbon.gateway.core.flow.contentaware.messagesourceimpl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.contentaware.xpath.CarbonXPathImpl;
import org.wso2.carbon.messaging.MessageDataSource;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * A Class which represents SOAP Messages
 */
public class CarbonSOAPMessageImpl implements MessageDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonSOAPMessageImpl.class);

    private OMElement omElement;
    private String contentType;
    private String charsetEncoding;

    public CarbonSOAPMessageImpl(OMElement omElement, String contentType) {
        this.omElement = omElement;
        this.contentType = contentType;
    }

    @Override
    public String getStringValue(String xPath) {
        try {
            Object result = null;
            OMElement eval = null;
            CarbonXPathImpl carbonXPath = new CarbonXPathImpl(xPath);
            if (omElement instanceof SOAPEnvelope) {
                eval = ((SOAPEnvelope) omElement).getBody().getFirstElement();
                result = carbonXPath.evaluate(eval);
            } else {
                result = carbonXPath.evaluate(omElement);
            }

            StringBuffer sb = new StringBuffer();
            if (result instanceof OMNode) {
                return result.toString();
            } else if (result instanceof List) {
                ((List) result).forEach(re -> sb.append(re.toString()));
                return sb.toString();
            }

        } catch (JaxenException e) {
            LOGGER.error("Error occurred while evaluating xpath", e);
        }
        return null;
    }

    @Override
    public Object getValue(String xPath) {
        try {
            OMElement eval = null;
            CarbonXPathImpl carbonXPath = new CarbonXPathImpl(xPath);
            if (omElement instanceof SOAPEnvelope) {
                eval = ((SOAPEnvelope) omElement).getBody().getFirstElement();
                return carbonXPath.evaluate(eval);
            } else {
                return carbonXPath.evaluate(omElement);
            }

        } catch (JaxenException e) {
            LOGGER.error("Error occurred while evaluating xpath", e);
        }
        return null;
    }

    @Override
    public Object getDataObject() {
        return omElement;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String s) {
        this.contentType = s;
    }

    public String getCharsetEncoding() {
        return charsetEncoding;
    }

    public void setCharsetEncoding(String charsetEncoding) {
        this.charsetEncoding = charsetEncoding;
    }

    @Override
    public ByteBuffer getDataAsByteBuffer() {
        try {
            return ByteBuffer.wrap(omElement.toString().getBytes(charsetEncoding));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Wrong CharSet Encoding", e);
        }
        return null;
    }
}
