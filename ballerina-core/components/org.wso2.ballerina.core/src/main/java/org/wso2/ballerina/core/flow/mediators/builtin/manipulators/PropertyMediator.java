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
package org.wso2.ballerina.core.flow.mediators.builtin.manipulators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.Constants;
import org.wso2.ballerina.core.config.ParameterHolder;
import org.wso2.ballerina.core.flow.AbstractMediator;
import org.wso2.ballerina.core.util.VariableUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Basic implementation of property mediator to declare and assign variables
 */
public class PropertyMediator extends AbstractMediator {

    private static final Logger log = LoggerFactory.getLogger(PropertyMediator.class);

    private String key;
    private String value;
    private Constants.TYPES type;
    private boolean assignment;
    private Object variable;

    public PropertyMediator() {}

    public PropertyMediator(String key, String value, Constants.TYPES type, boolean assignment) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.assignment = assignment;
        this.variable = VariableUtil.createVariable(type, value);
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

            Map<String, Object> map = (Map) VariableUtil.getMap(carbonMessage, key);
            if (map == null) {
                map = createAndPushMapIfNotExist(variableStack);
            }
            if (assignment) {
                type = VariableUtil.getType(VariableUtil.getVariable(carbonMessage, key));
            }
            variable = VariableUtil.createVariable(type, value);
            map.put(key, variable);

        } else {
            log.error("Variable stack has not been initialized!");
            return false;
        }

        return next(carbonMessage, carbonCallback);
    }

    private Map<String, Object> createAndPushMapIfNotExist(Stack<Map<String, Object>> variableStack) {
        Map<String, Object> map;
        if (variableStack.size() > 0) {
            map = variableStack.peek();
        } else {
            map = new HashMap<>();
            variableStack.push(map);
        }

        return map;
    }

    public void setParameters(ParameterHolder parameterHolder) {
        key = parameterHolder.getParameter("key").getValue();
        // value is null when variable declaration
        value = (parameterHolder.getParameter("value") != null) ?
                parameterHolder.getParameter("value").getValue() :
                null;
        // type is null when variable assignment
        type = (parameterHolder.getParameter("type") != null) ?
                VariableUtil.getType(parameterHolder.getParameter("type").getValue()) :
                null;
        assignment = Boolean.valueOf(parameterHolder.getParameter("assignment").getValue());
    }

}
