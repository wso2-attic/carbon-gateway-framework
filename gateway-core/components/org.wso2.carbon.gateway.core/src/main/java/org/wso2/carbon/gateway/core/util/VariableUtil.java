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
package org.wso2.carbon.gateway.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * This class is a Utility class that provies commons variable related functions like creating a new
 * variable stack and pushing this onto CarbonMessage variables stack.
 */
public class VariableUtil {

    private static final Logger log = LoggerFactory.getLogger(VariableUtil.class);


    /**
     * Creates a new variable stack containing global variables and attaches this to CarbonMessage.
     * @param cMsg
     */
    public static void pushGlobalVariableStack(CarbonMessage cMsg, Map<String, Object> globalVariables) {
        // check if stack exists in cMsg, create empty otherwise
        Stack<Map<String, Object>> variableStack;
        if (cMsg.getProperty(Constants.VARIABLE_STACK) != null) {
            variableStack = (Stack<Map<String, Object>>) cMsg.getProperty(Constants.VARIABLE_STACK);
        } else {
            variableStack = new Stack<Map<String, Object>>();
            cMsg.setProperty(Constants.VARIABLE_STACK, variableStack);
        }

        if (variableStack.size() == 0) {
            variableStack.push(globalVariables);
        } else {
            Map<String, Object> gtScope = variableStack.peek();
            globalVariables.put(Constants.GW_GT_SCOPE, gtScope);
            variableStack.push(globalVariables);
        }
    }

    /**
     * Creates a new variable stack with an empty variable map if the stack size is zero or else it will create a new
     * map with a reference to the map on top of the stack and push this new map onto the stack.
     * @param cMsg
     */
    public static void pushNewVariableStack(CarbonMessage cMsg) {
        // check if stack exists in cMsg, create empty otherwise
        Stack<Map<String, Object>> variableStack;
        if (cMsg.getProperty(Constants.VARIABLE_STACK) != null) {
            variableStack = (Stack<Map<String, Object>>) cMsg.getProperty(Constants.VARIABLE_STACK);
        } else {
            variableStack = new Stack<Map<String, Object>>();
            cMsg.setProperty(Constants.VARIABLE_STACK, variableStack);
        }

        if (variableStack.size() == 0) {
            variableStack.push(new HashMap<String, Object>());
        } else {
            Map<String, Object> newMap = new HashMap<>();
            newMap.put(Constants.GW_GT_SCOPE, variableStack.peek());
            variableStack.push(newMap);
        }
    }

    /**
     * Set variable stack to CarbonMessage if it does not already exist.
     * @param cMsg
     * @param stack
     */
    public static void setVariableStack(CarbonMessage cMsg, Stack<Map<String, Object>> stack) {
        if (cMsg.getProperty(Constants.VARIABLE_STACK) == null) {
            cMsg.setProperty(Constants.VARIABLE_STACK, stack);
        }
    }

    /**
     * Get variable stack from CarbonMessage. This method will create an empty variable stack if it does not exist.
     * @param cMsg
     * @return variable stack
     */
    public static Stack<Map<String, Object>> getVariableStack(CarbonMessage cMsg) {
        if (cMsg.getProperty(Constants.VARIABLE_STACK) != null) {
            return ((Stack<Map<String, Object>>) cMsg.getProperty(Constants.VARIABLE_STACK));
        } else {
            pushNewVariableStack(cMsg);
            return (Stack<Map<String, Object>>) cMsg.getProperty(Constants.VARIABLE_STACK);
        }
    }
}
