/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.AbstractMediator;
import org.wso2.carbon.gateway.core.util.VariableUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Basic implementation of property mediator to assign variables
 */
public class PropertyMediator extends AbstractMediator {

    private static final Logger log = LoggerFactory.getLogger(PropertyMediator.class);

    private String key;
    private String value;
    private String type;
    private Object variable;

    public PropertyMediator() {}

    public PropertyMediator(String key, String value, String type) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.variable = VariableUtil.getVariable(type, value);
    }

    @Override
    public String getName() {
        return "property";
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        if (carbonMessage.getProperty(Constants.VARIABLE_STACK) != null) {
            Stack<Map<String, Object>> variableStack =
                    (Stack<Map<String, Object>>) carbonMessage.getProperty(Constants.VARIABLE_STACK);

            Map<String, Object> map;
            if (variableStack.size() > 0) {
                map = variableStack.peek();
            } else {
                map = new HashMap<>();
                variableStack.push(map);
            }

            map.put(key, variable);
        } else {
            log.error("Variable stack has not been initialized!");
            return false;
        }

        return next(carbonMessage, carbonCallback);
    }

    public void setParameters(ParameterHolder parameterHolder) {
        key = parameterHolder.getParameter("key").getValue();
        value = parameterHolder.getParameter("value").getValue();
        type = parameterHolder.getParameter("type").getValue();
        variable = VariableUtil.getVariable(type, value);
    }

}
