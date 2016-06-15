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

package org.wso2.carbon.gateway.mediators.headermediator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.config.Parameter;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.AbstractMediator;
import org.wso2.carbon.gateway.core.flow.contentaware.messagereaders.Reader;
import org.wso2.carbon.gateway.core.flow.contentaware.messagereaders.ReaderRegistryImpl;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageDataSource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;

/**
 * A Header Mediator uses to mediate headers in transport level and soap header
 */
public class HeaderMediator extends AbstractMediator {

    private static final Logger logger = LoggerFactory.getLogger(HeaderMediator.class);

    private String name;
    private String value;
    private String expression;
    private Scope scope = Scope.TRANSPORT;
    private Action action = Action.SET;
    private String inlineXMLValue;
    private Map<String, String> properties = new HashMap<>();

    @Override
    public String getName() {
        return "header";
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        if (action == Action.REMOVE && scope == Scope.TRANSPORT) {
            carbonMessage.removeHeader(name);
        } else if (action == Action.SET && scope == Scope.TRANSPORT) {
            carbonMessage.setHeader(name, value);
        } else if (action == Action.REMOVE && scope == Scope.SOAP) {
            MessageDataSource messageDataSource = carbonMessage.getMessageDataSource();
            if (messageDataSource != null && messageDataSource.getDataObject() instanceof SOAPEnvelope) {
                removeSOAPHeader(messageDataSource);
            } else {
                Reader reader = ReaderRegistryImpl.getInstance().getReader(carbonMessage);
                messageDataSource = reader.makeMessageReadable(carbonMessage);
                removeSOAPHeader(messageDataSource);
            }
        } else if (action == Action.SET && scope == Scope.SOAP) {
            MessageDataSource messageDataSource = carbonMessage.getMessageDataSource();
            SOAPEnvelope soapEnvelope = null;
            if (messageDataSource == null) {
                Reader reader = ReaderRegistryImpl.getInstance().getReader(carbonMessage);
                if (reader == null) {
                    logger.error("Cannot find registered message reader for the incoming content type");
                    return false;
                }
                messageDataSource = reader.makeMessageReadable(carbonMessage);

            }
            if (messageDataSource.getDataObject() != null && messageDataSource
                    .getDataObject() instanceof SOAPEnvelope) {
                soapEnvelope = (SOAPEnvelope) messageDataSource.getDataObject();
            }
            addCustomHeader(messageDataSource, soapEnvelope);

        }
        return next(carbonMessage, carbonCallback);
    }

    @Override
    public void setParameters(ParameterHolder parameterHolder) {
        Parameter parameter = parameterHolder.getParameter("name");
        if (parameter != null) {
            name = parameter.getValue();
        }
        Parameter parameterValue = parameterHolder.getParameter("value");
        if (parameterValue != null) {
            value = parameterValue.getValue();
        }
        Parameter parameterScope = parameterHolder.getParameter("scope");
        if (parameterScope != null) {
            String value = parameterScope.getValue();
            if (value.equalsIgnoreCase("TRANSPORT")) {
                scope = Scope.TRANSPORT;
            } else {
                scope = Scope.SOAP;
            }
        }
        Parameter parameterInlineXML = parameterHolder.getParameter("inLineXML");
        if (parameterInlineXML != null) {
            inlineXMLValue = parameterInlineXML.getValue();
        }

        Parameter parameterXPath = parameterHolder.getParameter("xpath");
        if (parameterXPath != null) {
            expression = parameterXPath.getValue();
        }
        Parameter parameterAction = parameterHolder.getParameter("action");
        if (parameterAction != null) {
            String action = parameterAction.getValue();
            if (action.equalsIgnoreCase("SET")) {
                this.action = Action.SET;
            } else {
                this.action = Action.REMOVE;
            }
        }
        for (Map.Entry entry : parameterHolder.getParameters().entrySet()) {
            String key = (String) entry.getKey();
            String modifiedKey = null;
            Parameter parameterName = (Parameter) entry.getValue();
            String val = parameterName.getValue();
            if (key.startsWith("namespace=")) {
                modifiedKey = key.substring(("namespace=").length());
                properties.put(modifiedKey, val);
            }
        }

    }

    private void removeSOAPHeader(MessageDataSource messageDataSource) throws SOAPException {

        SOAPEnvelope soapEnvelope = (SOAPEnvelope) messageDataSource.getDataObject();
        SOAPHeader soapHeader = soapEnvelope.getHeader();
        if (soapHeader != null) {
            for (Iterator iter = soapHeader.examineAllHeaderBlocks(); iter.hasNext(); ) {
                Object o = iter.next();
                if (o instanceof SOAPHeaderBlock) {
                    SOAPHeaderBlock headerBlk = (SOAPHeaderBlock) o;
                    if (name.equals(headerBlk.getLocalName())) {
                        headerBlk.detach();
                    }
                } else if (o instanceof OMElement) {
                    OMElement headerElem = (OMElement) o;
                    if (name.equals(headerElem.getLocalName())) {
                        headerElem.detach();
                    }
                }
            }
        }
    }

    private void addCustomHeader(MessageDataSource messageDataSource, SOAPEnvelope env) throws XMLStreamException {
        if (env == null) {
            return;
        }
        SOAPFactory fac = (SOAPFactory) env.getOMFactory();
        SOAPHeader header = env.getHeader();
        if (header == null) {
            header = fac.createSOAPHeader(env);
        }
        if (inlineXMLValue == null && expression == null) {

            if (properties.size() == 1) {
                String prefix = null;
                String val = null;
                for (Map.Entry entry : properties.entrySet()) {
                    prefix = (String) entry.getKey();
                    val = (String) entry.getValue();
                }
                SOAPHeaderBlock hb = header.addHeaderBlock(name, fac.createOMNamespace(prefix, val));
                hb.setText(value);
            } else if (properties.size() > 1) {
                logger.error("Cannot have multiple namespace for single header block");
            } else {
                SOAPHeaderBlock hb = header.addHeaderBlock(name, fac.createOMNamespace(name, null));
                hb.setText(value);
            }

        } else if (inlineXMLValue != null) {
            OMElement omElement = AXIOMUtil.stringToOM(inlineXMLValue);
            header.addChild(omElement);
        } else if (expression != null) {
            String evaluatedVal = null;
            if (properties.size() > 0) {
                evaluatedVal = messageDataSource.getValueAsString(expression, properties);
            } else {
                evaluatedVal = messageDataSource.getValueAsString(expression);
            }
            if (evaluatedVal != null) {
                if (properties.size() == 1) {
                    String prefix = null;
                    String val = null;
                    for (Map.Entry entry : properties.entrySet()) {
                        prefix = (String) entry.getKey();
                        val = (String) entry.getValue();
                    }
                    SOAPHeaderBlock hb = header.addHeaderBlock(name, fac.createOMNamespace(prefix, val));
                    hb.setText(evaluatedVal);
                }
            }

        } else {
            logger.error("Inline XML value or value need to be specified");
        }
    }

    private enum Scope {
        TRANSPORT, SOAP;
    }

    private enum Action {
        SET, REMOVE;
    }

}
