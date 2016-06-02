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
package org.wso2.carbon.gateway.core.flow.mediators.builtin.manipulators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.AbstractMediator;
import org.wso2.carbon.gateway.core.flow.contentaware.messagereaders.Reader;
import org.wso2.carbon.gateway.core.flow.contentaware.messagereaders.ReaderRegistryImpl;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageDataSource;

/**
 * Basic implementation of log mediator
 * TODO: Not implemented yet
 */
public class LogMediator extends AbstractMediator {

    private static final Logger log = LoggerFactory.getLogger(LogMediator.class);

    private String logMessage = "Message received at LogMediator";

    private String expression;

    public LogMediator(String logMessage) {
        this.logMessage = logMessage;
    }

    public LogMediator() {
    }

    @Override
    public String getName() {
        return "log";
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        log.info(getValue(carbonMessage, logMessage).toString());
        MessageDataSource messageDataSource = null;
        String msg = null;
        if (!carbonMessage.isAlreadyRead()) {
            Reader reader = ReaderRegistryImpl.getInstance().getReader(carbonMessage);
            if (reader != null) {
                messageDataSource = reader.makeMessageReadable(carbonMessage);
            } else {
                String errmsg = "Cannot find registered message reader for incoming content Type";
                log.error(errmsg);
                throw new Exception(errmsg);
            }

        } else {
            messageDataSource = carbonMessage.getMessageDataSource();
        }
        if (expression != null) {
            msg = messageDataSource.getStringValue(expression);
        }
        log.info(msg);
        return next(carbonMessage, carbonCallback);
    }

    public void setParameters(ParameterHolder parameterHolder) {
        String value = parameterHolder.getParameter("parameters").getValue();
        if (value.contains("$xpath")) {
            expression = value.substring(value.indexOf(":") + 1);
        }

    }

}
