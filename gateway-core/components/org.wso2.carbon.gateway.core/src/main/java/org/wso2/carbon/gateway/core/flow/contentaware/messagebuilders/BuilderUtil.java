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

import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXParserConfiguration;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.util.Locale;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A utility class for Builders
 */
public class BuilderUtil {

    private static final Logger log = LoggerFactory.getLogger(BuilderUtil.class);

    public static final int BOM_SIZE = 4;


    public static StAXBuilder getPOXBuilder(InputStream inStream, String charSetEnc) throws XMLStreamException {
        StAXBuilder builder;
        // We use the StAXParserConfiguration.SOAP here as well because we don't want to allow
        // document type declarations (that potentially reference external entities), even
        // in plain XML messages.
        XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(StAXParserConfiguration.SOAP, inStream, charSetEnc);
        builder = new StAXOMBuilder(xmlreader);
        return builder;
    }

    /**
     * Convenience method to get a PushbackInputStream so that we can read the BOM
     *
     * @param is a regular InputStream
     * @return a PushbackInputStream wrapping the passed one
     */
    public static PushbackInputStream getPushbackInputStream(InputStream is) {
        return new PushbackInputStream(is, BOM_SIZE);
    }

    /**
     * Use the BOM Mark to identify the encoding to be used. Fall back to default encoding
     * specified
     *
     * @param is2             PushBackInputStream (it must be a pushback input stream so that we can
     *                        unread the BOM)
     * @param defaultEncoding default encoding style if no BOM
     * @return the selected character set encoding
     * @throws java.io.IOException
     */
    public static String getCharSetEncoding(PushbackInputStream is2, String defaultEncoding) throws IOException {
        String encoding;
        byte bom[] = new byte[BOM_SIZE];
        int n, unread;

        n = is2.read(bom, 0, bom.length);

        if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
            encoding = "UTF-8";
            if (log.isDebugEnabled()) {
                log.debug("char set encoding set from BOM =" + encoding);
            }
            unread = n - 3;
        } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
            encoding = "UTF-16BE";
            if (log.isDebugEnabled()) {
                log.debug("char set encoding set from BOM =" + encoding);
            }
            unread = n - 2;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
            encoding = "UTF-16LE";
            if (log.isDebugEnabled()) {
                log.debug("char set encoding set from BOM =" + encoding);
            }
            unread = n - 2;
        } else if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) && (bom[2] == (byte) 0xFE) && (bom[3]
                == (byte) 0xFF)) {
            encoding = "UTF-32BE";
            if (log.isDebugEnabled()) {
                log.debug("char set encoding set from BOM =" + encoding);
            }
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) && (bom[2] == (byte) 0x00) && (bom[3]
                == (byte) 0x00)) {
            encoding = "UTF-32LE";
            if (log.isDebugEnabled()) {
                log.debug("char set encoding set from BOM =" + encoding);
            }
            unread = n - 4;
        } else {

            // Unicode BOM mark not found, unread all bytes
            encoding = defaultEncoding;
            if (log.isDebugEnabled()) {
                log.debug("char set encoding set from default =" + encoding);
            }
            unread = n;
        }

        if (unread > 0) {
            is2.unread(bom, (n - unread), unread);
        }
        return encoding;
    }

    public static String getEnvelopeNamespace(String contentType) {
        String soapNS = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        if (contentType != null) {
            if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                // it is SOAP 1.2
                soapNS = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            } else if (contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
                // SOAP 1.1
                soapNS = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            }
        }
        return soapNS;
    }

    /**
     * /**
     * Utility method to get a StAXBuilder
     *
     * @param in an InputStream
     * @return a StAXSOAPModelBuilder for the given InputStream
     * @throws XMLStreamException
     * @deprecated If some one really need this method, please shout.
     */
    public static StAXBuilder getBuilder(Reader in) throws XMLStreamException {
        XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(in);
        return new StAXSOAPModelBuilder(xmlreader, null);
    }

    /**
     * Creates an OMBuilder for a plain XML message. Default character set encording is used.
     *
     * @param inStream InputStream for a XML message
     * @return Handler to a OMBuilder implementation instance
     * @throws XMLStreamException
     */
    public static StAXBuilder getBuilder(InputStream inStream) throws XMLStreamException {
        XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(inStream);
        return new StAXOMBuilder(xmlReader);
    }

    /**
     * Creates an OMBuilder for a plain XML message.
     *
     * @param inStream   InputStream for a XML message
     * @param charSetEnc Character set encoding to be used
     * @return Handler to a OMBuilder implementation instance
     * @throws XMLStreamException
     */
    public static StAXBuilder getBuilder(InputStream inStream, String charSetEnc) throws XMLStreamException {
        XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(inStream, charSetEnc);
        try {
            return new StAXSOAPModelBuilder(xmlReader);
        } catch (OMException e) {
            log.info("OMException in getSOAPBuilder", e);
            try {
                log.info("Remaining input stream :[" +
                        new String(IOUtils.getStreamAsByteArray(inStream), charSetEnc) + "]");
            } catch (IOException e1) {
                // Nothing here?
            }
            throw e;
        }
    }

    /**
     * Creates an OMBuilder for a SOAP message. Default character set encording is used.
     *
     * @param inStream InputStream for a SOAP message
     * @return Handler to a OMBuilder implementation instance
     * @throws XMLStreamException
     */
    public static StAXBuilder getSOAPBuilder(InputStream inStream) throws XMLStreamException {
        XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(inStream);
        try {
            return new StAXSOAPModelBuilder(xmlReader);
        } catch (OMException e) {
            log.info("OMException in getSOAPBuilder", e);
            try {
                log.info("Remaining input stream :[" +
                        new String(IOUtils.getStreamAsByteArray(inStream) , "UTF-8") + "]");
            } catch (IOException e1) {
                // Nothing here?
            }
            throw e;
        }
    }

    /**
     * Creates an OMBuilder for a SOAP message.
     *
     * @param inStream   InputStream for a SOAP message
     * @param charSetEnc Character set encoding to be used
     * @return Handler to a OMBuilder implementation instance
     * @throws XMLStreamException
     */
    public static StAXBuilder getSOAPBuilder(InputStream inStream, String charSetEnc) throws XMLStreamException {
        XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(inStream, charSetEnc);
        try {
            return new StAXSOAPModelBuilder(xmlReader);
        } catch (OMException e) {
            log.info("OMException in getSOAPBuilder", e);
            try {
                log.info("Remaining input stream :[" +
                        new String(IOUtils.getStreamAsByteArray(inStream), charSetEnc) + "]");
            } catch (IOException e1) {
                // Nothing here?
            }
            throw e;
        }
    }

    public static StAXBuilder getBuilder(SOAPFactory soapFactory, InputStream in, String charSetEnc)
            throws XMLStreamException {
        StAXBuilder builder;
        XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(in, charSetEnc);
        builder = new StAXOMBuilder(soapFactory, xmlreader);
        return builder;
    }

    public static void validateSOAPVersion(String soapNamespaceURIFromTransport, SOAPEnvelope envelope) {
        if (soapNamespaceURIFromTransport != null) {
            OMNamespace envelopeNamespace = envelope.getNamespace();
            String namespaceName = envelopeNamespace.getNamespaceURI();
            if (!(soapNamespaceURIFromTransport.equals(namespaceName))) {
                throw new SOAPProcessingException(
                        "Transport level information does not match with SOAP" + " Message namespace URI",
                        envelopeNamespace.getPrefix() + ":" +
                                SOAPConstants.FAULT_CODE_VERSION_MISMATCH);
            }
        }
    }

    public static void validateCharSetEncoding(String charsetEncodingFromTransport, String charsetEncodingFromXML,
            String soapNamespaceURI) {
        if ((charsetEncodingFromXML != null) && !"".equals(charsetEncodingFromXML) && (charsetEncodingFromTransport
                != null) && !charsetEncodingFromXML.equalsIgnoreCase(charsetEncodingFromTransport)
                && !compatibleEncodings(charsetEncodingFromXML, charsetEncodingFromTransport)) {
            /**
             * WS-BP Rule 1019 requires toleration if the character sets mismatch
             * I am changing this to simply a debug statement.
             String faultCode;

             if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapNamespaceURI)) {
             faultCode = SOAP12Constants.FAULT_CODE_SENDER;
             } else {
             faultCode = SOAP11Constants.FAULT_CODE_SENDER;
             }

             throw new AxisFault("Character Set Encoding from "
             + "transport information [" + charsetEncodingFromTransport + "] does not match with "
             + "character set encoding in the received SOAP message [" + charsetEncodingFromXML + "]", faultCode);
             **/
            if (log.isDebugEnabled()) {
                log.debug("Character Set Encoding from " + "transport information [" + charsetEncodingFromTransport
                        + "] does not match with " + "character set encoding in the received SOAP message ["
                        + charsetEncodingFromXML + "]");
            }
        }
    }

    /**
     * check if the pair is [UTF-16,UTF-16LE] [UTF-32, UTF-32LE],[UTF-16,UTF-16BE] [UTF-32,
     * UTF-32BE] etc.
     *
     * @param enc1 encoding style
     * @param enc2 encoding style
     * @return true if the encoding styles are compatible, or false otherwise
     */
    private static boolean compatibleEncodings(String enc1, String enc2) {
        enc1 = enc1.toLowerCase(Locale.getDefault());
        enc2 = enc2.toLowerCase(Locale.getDefault());
        if (enc1.endsWith("be") || enc1.endsWith("le")) {
            enc1 = enc1.substring(0, enc1.length() - 2);
        }
        if (enc2.endsWith("be") || enc2.endsWith("le")) {
            enc2 = enc2.substring(0, enc2.length() - 2);
        }
        return enc1.equals(enc2);
    }
}
