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
package org.wso2.ballerina.core.flow.mediators.builtin.manipulators.log;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.Constants;
import org.wso2.ballerina.core.config.Parameter;
import org.wso2.ballerina.core.config.ParameterHolder;
import org.wso2.ballerina.core.flow.AbstractMediator;
import org.wso2.ballerina.core.flow.contentaware.MIMEType;
import org.wso2.ballerina.core.flow.contentaware.messagereaders.Reader;
import org.wso2.ballerina.core.flow.contentaware.messagereaders.ReaderRegistryImpl;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Implementation of Log Mediator
 */
public class LogMediator extends AbstractMediator {

    private static final String CATEGORY = "category";
    private static final String LEVEL = "level";
    private static final String SEPARATOR = "separator";

    /**
     * Only properties specified to the Log mediator
     */
    public static final int CUSTOM = 0;
    /**
     * To, From, WSAction, SOAPAction, ReplyTo, MessageID and any properties
     */
    public static final int SIMPLE = 1;
    /**
     * All SOAP header blocks and any properties
     */
    public static final int HEADERS = 2;
    /**
     * all attributes of level 'simple' and the SOAP envelope and any properties
     */
    public static final int FULL = 3;

    public static final int CATEGORY_INFO = 0;
    public static final int CATEGORY_DEBUG = 1;
    public static final int CATEGORY_TRACE = 2;
    public static final int CATEGORY_WARN = 3;
    public static final int CATEGORY_ERROR = 4;
    public static final int CATEGORY_FATAL = 5;

    public static final String DEFAULT_SEP = ", ";

    /**
     * The default log level is set to SIMPLE
     */
    private int logLevel = SIMPLE;
    /**
     * The separator for which used to separate logging information
     */
    private String separator = DEFAULT_SEP;
    /**
     * Category of the log statement
     */
    private int category = CATEGORY_INFO;
    /**
     * The holder for the custom properties
     */
    private final List<LogMediatorProperty> properties = new ArrayList<>();
    /**
     * Message ID
     */
    private String messageId;
    /**
     * Message reader is required only if the mediator tries to access the content of the message.
     * In Log mediator, corresponding message reader is requested only if the log-level is full or an expression is
     * present.
     */
    private Boolean readerRequired = false;

    private static final Logger log = LoggerFactory.getLogger(LogMediator.class);

    public LogMediator() {
    }

    @Override
    public String getName() {
        return "log";
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        boolean trace = category == 2 ? true : false;
        MediatorLog mediatorLog = new MediatorLog(log, trace, carbonMessage);
        CarbonMessage carbonMessageRef = carbonMessage;
        /* If the messageRef is provided in the configuration, lookup in the variable stack for that message.
        * And if its found, use it to log operation. Else skip this mediator while giving an error.
        * In case where messageRef is not specified in the configuration, skip the lookup, move on with the existing
        * carbon message.
        * */
        if (messageId != null) {
            Object carbonMessageObject = getObjectFromContext(carbonMessage, messageId);
            if (!(carbonMessageObject instanceof CarbonMessage)) {
                log.error("Skipping the log due to message variable named " + messageId + " not found in the context.");
                return next(carbonMessage, carbonCallback);
            }
            carbonMessageRef = (CarbonMessage) carbonMessageObject;
        }
        Reader reader = null;
        if (readerRequired && !carbonMessageRef.isAlreadyRead()) {
            reader = ReaderRegistryImpl.getInstance().getReader(carbonMessageRef);
            if (reader == null) {
                String errMsg = "Cannot find registered message reader for incoming content Type";
                log.error(errMsg);
                throw new Exception(errMsg);
            }
        }
        switch (category) {
        case CATEGORY_INFO:
            mediatorLog.auditLog(getLogMessage(carbonMessageRef, reader));
            break;
        case CATEGORY_TRACE:
            if (mediatorLog.isTraceEnabled()) {
                mediatorLog.auditTrace(getLogMessage(carbonMessageRef, reader));
            }
            break;
        case CATEGORY_DEBUG:
            if (mediatorLog.isDebugEnabled()) {
                mediatorLog.auditDebug(getLogMessage(carbonMessageRef, reader));
            }
            break;
        case CATEGORY_WARN:
            mediatorLog.auditWarn(getLogMessage(carbonMessageRef, reader));
            break;
        case CATEGORY_ERROR:
            mediatorLog.auditError(getLogMessage(carbonMessageRef, reader));
            break;
        case CATEGORY_FATAL:
            mediatorLog.auditFatal(getLogMessage(carbonMessageRef, reader));
            break;
        default:
            break;
        }
        return next(carbonMessage, carbonCallback);
    }

    public void setParameters(ParameterHolder parameterHolder) {

        Parameter levelParameter = parameterHolder.getParameter(LEVEL);
        Parameter catageryParameter = parameterHolder.getParameter(CATEGORY);
        Parameter seperatorParameter = parameterHolder.getParameter(SEPARATOR);

        if (levelParameter != null) {
            if (levelParameter.getValue().toUpperCase(Locale.getDefault()).equals("SIMPLE")) {
                logLevel = SIMPLE;
            } else if (levelParameter.getValue().toUpperCase(Locale.getDefault()).equals("CUSTOM")) {
                logLevel = CUSTOM;
            } else if (levelParameter.getValue().toUpperCase(Locale.getDefault()).equals("HEADERS")) {
                logLevel = HEADERS;
                readerRequired = true;
            } else if (levelParameter.getValue().toUpperCase(Locale.getDefault()).equals("FULL")) {
                logLevel = FULL;
                readerRequired = true;
            }
            parameterHolder.removeParameter(levelParameter.getName());
        }
        if (catageryParameter != null) {
            if (catageryParameter.getValue().toUpperCase(Locale.getDefault()).equals("INFO")) {
                category = CATEGORY_INFO;
            } else if (catageryParameter.getValue().toUpperCase(Locale.getDefault()).equals("ERROR")) {
                category = CATEGORY_ERROR;
            } else if (catageryParameter.getValue().toUpperCase(Locale.getDefault()).equals("WARN")) {
                category = CATEGORY_WARN;
            } else if (catageryParameter.getValue().toUpperCase(Locale.getDefault()).equals("FATAL")) {
                category = CATEGORY_FATAL;
            } else if (catageryParameter.getValue().toUpperCase(Locale.getDefault()).equals("DEBUG")) {
                category = CATEGORY_DEBUG;
            } else if (catageryParameter.getValue().toUpperCase(Locale.getDefault()).equals("TRACE")) {
                category = CATEGORY_TRACE;
            }
            parameterHolder.removeParameter(catageryParameter.getName());
        }
        if (seperatorParameter != null) {
            separator = seperatorParameter.getValue();
            parameterHolder.removeParameter(seperatorParameter.getName());
        }
        // remove the Integration Name from the logging parameters
        parameterHolder.removeParameter(Constants.INTEGRATION_KEY);

        // Setting the message id
        if (parameterHolder.getParameter(Constants.MESSAGE_KEY) != null) {
            this.messageId = parameterHolder.getParameter(Constants.MESSAGE_KEY).getValue();
            // remove message reference from logging parameters
            parameterHolder.removeParameter(Constants.MESSAGE_KEY);
        }

        Map<String, Parameter> properties = parameterHolder.getParameters();
        for (Map.Entry entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            Parameter parameter = (Parameter) entry.getValue();
            String val = parameter.getValue();
            String expression = null;
            if (val.startsWith("xpath=")) {
                expression = val.substring(("xpath=").length());

            } else if (val.startsWith("jsonPath=")) {
                expression = val.substring(("jsonPath=").length());
            }
            if (expression != null) {
                Map<String, String> map = getNameSpaceMap(properties);
                if (map != null) {
                    LogMediatorProperty logMediatorProperty = new LogMediatorProperty(key, null, expression, map);
                    this.properties.add(logMediatorProperty);
                } else {
                    LogMediatorProperty logMediatorProperty = new LogMediatorProperty(key, null, expression);
                    this.properties.add(logMediatorProperty);
                }
                readerRequired = true;

            } else if (!key.startsWith("namespace=")) {
                LogMediatorProperty logMediatorProperty = new LogMediatorProperty(key, val, null);
                this.properties.add(logMediatorProperty);
            }

        }

    }

    private String getCustomLogMessage(CarbonMessage carbonMessage, Reader reader) throws Exception {
        StringBuffer sb = new StringBuffer();
        setCustomProperties(sb, carbonMessage, reader);
        return trimLeadingSeparator(sb);
    }

    private String getSimpleLogMessage(CarbonMessage carbonMessage, Reader reader) throws Exception {
        StringBuffer sb = new StringBuffer();
        if (carbonMessage.getHeader(org.wso2.carbon.messaging.Constants.TO) != null) {
            sb.append("To: ").append(carbonMessage.getHeader(org.wso2.carbon.messaging.Constants.TO));
        } else if (carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.TO) != null) {
            sb.append("To: ").append(carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.TO));
        } else {
            sb.append("To: ");
            if (carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.HOST) != null
                    && carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.PORT) != null) {
                String receivedFrom = carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.HOST) + ":" +
                        carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.PORT);
                sb.append(separator).append("FROM: ").append(receivedFrom);
            }
            if (carbonMessage.getProperty("WSAction") != null) {
                sb.append(separator).append("WSAction: ").append(carbonMessage.getProperty("WSAction"));
            }
            if (carbonMessage.getProperty(Constants.SOAPACTION) != null) {
                sb.append(separator).append("SOAPAction: ").append(carbonMessage.getProperty(Constants.SOAPACTION));
            }
            if (carbonMessage.getProperty("ReplyTo") != null) {
                sb.append(separator).append("ReplyTo: ").append(carbonMessage.getProperty("ReplyTo"));
            }
            if (carbonMessage.getProperty("MessageID") != null) {
                sb.append(separator).append("MessageID: ").append(carbonMessage.getProperty("MessageID"));
                sb.append(separator).append("Direction: ")
                        .append(carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION) != null ?
                                carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION) :
                                "request");
                setCustomProperties(sb, carbonMessage, reader);
            }
        }

        return

                trimLeadingSeparator(sb);

    }

    private String getHeadersLogMessage(CarbonMessage carbonMessage, Reader reader) throws Exception {
        StringBuffer sb = new StringBuffer();
        MessageDataSource messageDataSource = carbonMessage.getMessageDataSource();
        if (messageDataSource == null) {
            messageDataSource = reader.makeMessageReadable(carbonMessage);
        }
        if (messageDataSource.getDataObject() != null && messageDataSource.getDataObject() instanceof OMElement) {
            OMElement omElement = (OMElement) messageDataSource.getDataObject();
            if (omElement instanceof SOAPEnvelope) {
                try {
                    SOAPHeader header = (SOAPHeader) ((SOAPEnvelope) omElement).getHeader();
                    if (header != null) {
                        for (Iterator iter = header.examineAllHeaderBlocks(); iter.hasNext(); ) {
                            Object o = iter.next();
                            if (o instanceof SOAPHeaderBlock) {
                                SOAPHeaderBlock headerBlk = (SOAPHeaderBlock) o;
                                sb.append(separator).append(headerBlk.getLocalName()).
                                        append(" : ").append(headerBlk.getText());
                            } else if (o instanceof OMElement) {
                                OMElement headerElem = (OMElement) o;
                                sb.append(separator).append(headerElem.getLocalName()).
                                        append(" : ").append(headerElem.getText());
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error("Exception occurred while processing SOAPHeader", e);
                    return null;
                }

            }
        }
        setCustomProperties(sb, carbonMessage, reader);
        return trimLeadingSeparator(sb);
    }

    private String getFullLogMessage(CarbonMessage carbonMessage, Reader reader) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(getSimpleLogMessage(carbonMessage, reader));
        MessageDataSource messageDataSource = carbonMessage.getMessageDataSource();
        if (messageDataSource == null) {
            messageDataSource = reader.makeMessageReadable(carbonMessage);
        }
        if (isJSONMessage(messageDataSource)) {
            sb.append(separator).append("Payload: ").append(messageDataSource.getValueAsString("$"));
        } else if (isSOAPMessage(messageDataSource)) {
            sb.append(separator).append("Envelope: ").append(messageDataSource.getDataObject().toString());
        }

        return trimLeadingSeparator(sb);
    }

    private String getLogMessage(CarbonMessage carbonMessage, Reader reader) throws Exception {
        switch (logLevel) {
        case CUSTOM:
            return getCustomLogMessage(carbonMessage, reader);
        case SIMPLE:
            return getSimpleLogMessage(carbonMessage, reader);
        case HEADERS:
            return getHeadersLogMessage(carbonMessage, reader);
        case FULL:
            return getFullLogMessage(carbonMessage, reader);
        default:
            return "Invalid log level specified";
        }

    }

    private void setCustomProperties(StringBuffer sb, CarbonMessage carbonMessage, Reader reader) throws Exception {
        if (properties != null && !properties.isEmpty()) {
            for (LogMediatorProperty property : properties) {
                if (property != null) {
                    if (property.getValue() != null) {
                        sb.append(separator).append(property.getKey()).append(" = ")
                                .append(getValue(carbonMessage, property.getValue()));
                    } else {
                        if (carbonMessage.getMessageDataSource() != null) {
                            sb.append(separator).append(property.getKey()).append(" = ")
                                    .append(property.getNameSpaceMap() == null ?
                                            carbonMessage.getMessageDataSource()
                                                    .getValueAsString(property.getExpression()) :
                                            carbonMessage.getMessageDataSource()
                                                    .getValueAsString(property.getExpression(),
                                                            property.getNameSpaceMap()));
                        } else {
                            MessageDataSource messageDataSource = reader.makeMessageReadable(carbonMessage);
                            sb.append(separator).append(property.getKey()).append(" = ")
                                    .append(property.getNameSpaceMap() == null ?
                                            messageDataSource.getValueAsString(property.getExpression()) :
                                            messageDataSource.getValueAsString(property.getExpression(),
                                                    property.getNameSpaceMap()));
                        }
                    }
                }
            }
        }
    }

    private String trimLeadingSeparator(StringBuffer sb) {
        String retStr = sb.toString();
        if (retStr.startsWith(separator)) {
            return retStr.substring(separator.length());
        } else {
            return retStr;
        }
    }

    private boolean isSOAPMessage(MessageDataSource messageDataSource) {
        if (messageDataSource.getContentType().equals(MIMEType.APPLICATION_XML) ||
                messageDataSource.getContentType().equals(MIMEType.APPLICATION_SOAP_XML) ||
                messageDataSource.getContentType().equals(MIMEType.TEXT_XML)) {
            return true;
        }
        return false;
    }

    private boolean isJSONMessage(MessageDataSource messageDataSource) {
        if (messageDataSource.getContentType().equals(MIMEType.APPLICATION_JSON)) {
            return true;
        }
        return false;
    }

    private Map<String, String> getNameSpaceMap(Map<String, Parameter> parameterMap) {
        Map<String, String> nameSpaceMap = null;
        for (Map.Entry entry : parameterMap.entrySet()) {
            String key = (String) entry.getKey();
            String modifiedKey = null;
            Parameter parameter = (Parameter) entry.getValue();
            String val = parameter.getValue();
            if (key.startsWith("namespace=")) {
                modifiedKey = key.substring(("namespace=").length());
                if (nameSpaceMap == null) {
                    nameSpaceMap = new HashMap<>();
                }
                nameSpaceMap.put(modifiedKey, val);
            }
        }
        return nameSpaceMap;
    }
}
