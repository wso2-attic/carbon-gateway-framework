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

package org.wso2.carbon.gateway.message.readers.xmlreader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.util.DetachableInputStream;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.flow.contentaware.MIMEType;
import org.wso2.carbon.gateway.core.flow.contentaware.messagereaders.AbstractReader;
import org.wso2.carbon.gateway.core.flow.contentaware.messagereaders.ReaderUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;


/**
 * A class which builds SOAPMessage
 */
public class SOAPReader extends AbstractReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOAPReader.class);

    private static final String CHARSET = "charset";

    public SOAPReader(String contentType) {
        super(contentType);
    }

    public MessageDataSource makeMessageReadable(CarbonMessage carbonMessage) throws IOException {
        SOAPFactory soapFactory = null;
        SOAPEnvelope envelope = null;
        PushbackInputStream pis = null;
        OutputStream outputStream = carbonMessage.getOutputStream();
        String charset = null;
        InputStream inputStream = carbonMessage.getInputStream();
        String contentType = carbonMessage.getHeader(Constants.HTTP_CONTENT_TYPE);

        if (contentType == null) {
            contentType = MIMEType.TEXT_XML;
        } else {
            contentType = ReaderUtil.parseContentType(contentType);
            charset = ReaderUtil.parseCharset(contentType);
        }
        try {

            carbonMessage.setProperty(Constants.CHARACTER_SET_ENCODING, charset);

            // Apply a detachable inputstream.  This can be used later
            // to  get the length of the incoming message
            DetachableInputStream is = new DetachableInputStream(inputStream);
            carbonMessage.setProperty(Constants.DETACHABLE_INPUT_STREAM, is);

            // Get the actual encoding by looking at the BOM of the InputStream
            pis = XMLUtil.getPushbackInputStream(is);
            int bytesRead = pis.read();
            if (bytesRead != -1) {
                pis.unread(bytesRead);
                String actualCharSetEncoding = XMLUtil.getCharSetEncoding(pis, charset);
                OMXMLParserWrapper builder = OMXMLBuilderFactory.createSOAPModelBuilder(pis, actualCharSetEncoding);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
                XMLUtil.validateSOAPVersion(XMLUtil.getEnvelopeNamespace(contentType), envelope);
                XMLUtil.validateCharSetEncoding(charset, builder.getDocument().getCharsetEncoding(),
                        envelope.getNamespace().getNamespaceURI());
            } else {
                if (contentType != null) {
                    if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                        soapFactory = OMAbstractFactory.getSOAP12Factory();
                    } else if (contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1 || isRESTRequest(
                            contentType)) {
                        soapFactory = OMAbstractFactory.getSOAP11Factory();
                    }
                }
                if (soapFactory != null) {
                    envelope = soapFactory.getDefaultEnvelope();
                }
            }
        } catch (IOException e) {
            String msg = "Error occurred while building message using SOAPBuilder for content type " + contentType;
            LOGGER.error(msg, e);
            pis.close();
            throw new IOException(msg, e);
        }
        CarbonXMLMessageImpl carbonSOAPMessage = new CarbonXMLMessageImpl(envelope, contentType, outputStream);
        carbonSOAPMessage.setCharsetEncoding(charset);
        attachMessageDataSource(carbonSOAPMessage, carbonMessage);
        return carbonSOAPMessage;
    }

    private boolean isRESTRequest(String contentType) {
        return contentType != null && (contentType.indexOf(Constants.MEDIA_TYPE_APPLICATION_XML) > -1 ||
                contentType.indexOf(Constants.MEDIA_TYPE_X_WWW_FORM) > -1 ||
                contentType.indexOf(Constants.MEDIA_TYPE_MULTIPART_FORM_DATA) > -1 ||
                contentType.indexOf(Constants.MEDIA_TYPE_APPLICATION_JSON) > -1);
    }

}
