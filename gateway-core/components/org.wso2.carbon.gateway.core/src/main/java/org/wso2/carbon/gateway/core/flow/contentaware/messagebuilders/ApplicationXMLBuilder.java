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

package org.wso2.carbon.gateway.core.flow.contentaware.messagebuilders;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.impl.OMNodeEx;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXParserConfiguration;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.flow.contentaware.MIMEType;
import org.wso2.carbon.gateway.core.flow.contentaware.messagesourceimpl.CarbonSOAPMessageImpl;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Locale;


import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

/**
 * A builder class for application/xml contentType
 */
public class ApplicationXMLBuilder extends AbstractBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationXMLBuilder.class);

    private final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private static final String CHARSET = "charset";

    public ApplicationXMLBuilder(String contentType) {
        super(contentType);
        inputFactory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
        inputFactory.setProperty("javax.xml.stream.isReplacingEntityReferences", Boolean.FALSE);
        inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
    }

    @Override
    public MessageDataSource processDocument(CarbonMessage carbonMessage) throws IOException, XMLStreamException {
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        InputStream inputStream = carbonMessage.getInputStream();
        PushbackInputStream pushbackInputStream = null;
        String charset = null;
        String contentType = carbonMessage.getHeader(Constants.HTTP_CONTENT_TYPE);
        if (contentType == null) {
            contentType = MIMEType.APPLICATION_XML;
        }
        try {

            if (contentType.toLowerCase(Locale.getDefault()).contains(CHARSET)) {
                String[] splitted = contentType.split(";");
                if (splitted.length > 0) {
                    contentType = splitted[0];
                    charset = splitted[1].substring(splitted[1].indexOf("=") + 1);
                }
            } else {
                charset = "UTF-8";
            }
            carbonMessage.setProperty(Constants.CHARACTER_SET_ENCODING, charset);
            if (inputStream != null) {
                try {
                    pushbackInputStream = new PushbackInputStream(inputStream);
                    int b;
                    if ((b = pushbackInputStream.read()) > 0) {
                        pushbackInputStream.unread(b);
                        javax.xml.stream.XMLStreamReader xmlReader;

                        if ("true".equals(carbonMessage.getProperty(Constants.APPLICATION_XML_BUILDER_ALLOW_DTD))) {
                            xmlReader = inputFactory.createXMLStreamReader(pushbackInputStream, charset);
                        } else {
                            xmlReader = StAXUtils
                                    .createXMLStreamReader(StAXParserConfiguration.SOAP, pushbackInputStream, charset);
                        }
                        StAXBuilder builder = new StAXOMBuilder(xmlReader);
                        OMNodeEx documentElement = (OMNodeEx) builder.getDocumentElement();
                        documentElement.setParent(null);
                        SOAPBody body = soapEnvelope.getBody();
                        body.addChild(documentElement);

                    }
                } catch (XMLStreamException e) {
                    String msg = "Error occurred while processing XML Streaming in ApplicationXMLBuilder";
                    LOGGER.error(msg, e);
                    pushbackInputStream.close();
                    throw new XMLStreamException(msg, e);
                }
            }
        } catch (IOException e) {
            String msg = "Error occurred while reading InputStream";
            LOGGER.error(msg, e);
            pushbackInputStream.close();
            throw new IOException(msg, e);
        }
        CarbonSOAPMessageImpl carbonSOAPMessage = new CarbonSOAPMessageImpl(soapEnvelope, contentType);
        carbonSOAPMessage.setCharsetEncoding(charset);
        attachMessageDataSource(carbonSOAPMessage, carbonMessage);
        return carbonSOAPMessage;
    }

}
