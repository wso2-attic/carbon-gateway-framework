/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.gateway.core.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.contentaware.ConversionManager;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Map;
import java.util.Stack;

/**
 * Base class for all the mediators. All the mediators must be extended from this base class
 */
public abstract class AbstractMediator implements Mediator {

    private static final Logger log = LoggerFactory.getLogger(AbstractMediator.class);


    /* Pointer for the next sibling in the pipeline*/
    Mediator nextMediator = null;

    /**
     * Check whether a sibling is present after this in the pipeline
     *
     * @return whether a sibling is present after this
     */
    public boolean hasNext() {
        return nextMediator != null;
    }

    /**
     * Invoke the next sibling in the pipeline
     *
     * @param carbonMessage  Carbon message
     * @param carbonCallback Incoming Callback
     * @return whether mediation is proceeded
     * @throws Exception
     */
    public boolean next(CarbonMessage carbonMessage, CarbonCallback carbonCallback)
            throws Exception {
        return hasNext() && nextMediator.receive(carbonMessage, carbonCallback);
    }

    /**
     * Set the pointer to the next sibling in the pipeline
     *
     * @param nextMediator Next sibling mediator in the pipeline
     */
    public void setNext(Mediator nextMediator) {
        this.nextMediator = nextMediator;
    }

    /**
     * Set Mediator Configurations
     *
     * @param parameterHolder Parameters Holder
     */
    public void setParameters(ParameterHolder parameterHolder) {
        //Do nothing
    }

    /**
     * Convert message into a specified format
     *
     * @param cMsg       Carbon Message
     * @param targetType Type to be converted
     * @return CarbonMessage with converted message body
     * @throws Exception
     */
    public CarbonMessage convertTo(CarbonMessage cMsg, String targetType) throws Exception {

        String sourceType = cMsg.getHeader("Content-Type");
        if (sourceType == null) {
            handleException("Content-Type header could not be found in the request");
            return null; // to make findbugs happy
        }
        sourceType = sourceType.split(";")[0];  // remove charset from Content-Type header

        return ConversionManager.getInstance().convertTo(cMsg, sourceType, targetType);
    }

    public void handleException(String msg) throws Exception {
        handleException(msg, null);
    }

    public void handleException(String msg, Exception ex) throws Exception {
        if (ex != null) {
            log.error(msg, ex);
            throw new Exception(msg, ex);
        } else {
            log.error(msg);
            throw new Exception(msg);
        }
    }

    public Object getValue(CarbonMessage carbonMessage, String name) {
        if (name.startsWith("$")) {
            Stack<Map<String, Object>> variableStack =
                    (Stack<Map<String, Object>>) carbonMessage.getProperty(Constants.VARIABLE_STACK);
            return findVariableValue(variableStack.peek(), name.substring(1));
        } else {
            return name;
        }
    }

    private Object findVariableValue(Map<String, Object> variables, String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else {
            if (variables.containsKey(Constants.GW_GT_SCOPE)) {
                return findVariableValue((Map<String, Object>) variables.get(Constants.GW_GT_SCOPE), name);
            } else {
                return null;
            }
        }
    }
}
