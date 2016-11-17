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

package org.wso2.ballerina.mediators.xmltojsontestmediator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.config.ParameterHolder;
import org.wso2.ballerina.core.flow.AbstractMediator;
import org.wso2.ballerina.core.flow.contentaware.MIMEType;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Sample XML to JSON Conversion Mediator
 */
public class XMLtoJSONTestMediator extends AbstractMediator {

    private static final Logger log = LoggerFactory.getLogger(XMLtoJSONTestMediator.class);
    private String logMessage = "Message received at Sample XML to JSON Mediator";

    @Override
    public String getName() {
        return "XMLtoJSONMediator";
    }

    /**
     * Mediate the message.
     * <p/>
     * This is the execution point of the mediator.
     *
     * @param carbonMessage  MessageContext to be mediated
     * @param carbonCallback Callback which can be use to call the previous step
     * @return whether mediation is success or not
     **/
    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {

        CarbonMessage convertedMsg = convertTo(carbonMessage, MIMEType.JSON);

        //CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        //CharBuffer charBuf = decoder.decode(convertedMsg.getMessageBody());
        //String msgBody = new String(charBuf.array());

        //log.info("\n" + msgBody);

        return next(convertedMsg, carbonCallback);
    }

    /**
     * Set Parameters
     *
     * @param parameterHolder holder which contains key-value pairs of parameters
     */
    @Override
    public void setParameters(ParameterHolder parameterHolder) {
        logMessage = parameterHolder.getParameter("parameters").getValue();
    }

    /**
     * This is a sample mediator specific method
     */
    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }
}
