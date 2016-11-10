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
import org.wso2.carbon.messaging.DefaultCarbonMessage;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

/**
 * Utility class that provides commons variable related functions like creating a new variable map and pushing
 * it onto CarbonMessage variables stack.
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
     * Pop top item off variable stack.
     * @param cMsg
     * @param stack
     */
    public static void popVariableStack(CarbonMessage cMsg, Stack<Map<String, Object>> stack) {
        if (cMsg.getProperty(Constants.VARIABLE_STACK) == null) {
            cMsg.setProperty(Constants.VARIABLE_STACK, stack);
        } else {
            Stack<Map<String, Object>> existingStack =
                    (Stack<Map<String, Object>>) cMsg.getProperty(Constants.VARIABLE_STACK);
            existingStack.pop();
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
     * Adds a variable onto the top most variable stack.
     * @param cMsg
     * @param key
     * @param variable
     */
    public static void addVariable(CarbonMessage cMsg, String key, Object variable) {
        Stack<Map<String, Object>> stack = getVariableStack(cMsg);
        stack.peek().put(key, variable);
    }

    /**
     * Add a variable onto the global constants stack.
     * @param cMsg
     * @param key
     * @param variable
     */
    public static void addGlobalVariable(CarbonMessage cMsg, String key, Object variable) {
        Stack<Map<String, Object>> stack = getVariableStack(cMsg);
        Map<String, Object> global;
        if (stack.peek().containsKey(Constants.GW_GT_SCOPE)) {
            global = (Map<String, Object>) stack.peek().get(Constants.GW_GT_SCOPE);
        } else {
            global = stack.peek();
        }

        global.put(key, variable);
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

    /**
     * Provides instantiated object of give variable type.
     * @param type
     * @param value
     * @return Object of variable type
     */
    public static Object createVariable(Constants.TYPES type, String value) {
        if (type.equals(Constants.TYPES.STRING)) {
            return String.valueOf(value);
        } else if (type.equals(Constants.TYPES.INTEGER)) {
            return Integer.valueOf(value);
        } else if (type.equals(Constants.TYPES.BOOLEAN)) {
            return Boolean.valueOf(value);
        } else if (type.equals(Constants.TYPES.DOUBLE)) {
            return Double.valueOf(value);
        } else if (type.equals(Constants.TYPES.FLOAT)) {
            return Float.valueOf(value);
        } else if (type.equals(Constants.TYPES.LONG)) {
            return Long.valueOf(value);
        } else if (type.equals(Constants.TYPES.SHORT)) {
            return Short.valueOf(value);
        } else if (type.equals(Constants.TYPES.MESSAGE)) {
            return new DefaultCarbonMessage();
        } else if (type.equals(Constants.TYPES.XML)) {
            log.info("XML Variable type not yet implemented! Using string instead.");
            return String.valueOf(value);
        } else if (type.equals(Constants.TYPES.JSON)) {
            log.info("JSON Variable type not yet implemented! Using string instead.");
            return String.valueOf(value);
        } else {
            log.error("Unrecognized variable type " + type);
            return null;
        }
    }

    /**
     * Returns the type of a variable object.
     * @param variable
     * @return object type as Constants.TYPES
     */
    public static Constants.TYPES getType(Object variable) {
        if (variable instanceof String) {
            return Constants.TYPES.STRING;
        } else if (variable instanceof Integer) {
            return Constants.TYPES.INTEGER;
        } else if (variable instanceof Boolean) {
            return Constants.TYPES.BOOLEAN;
        } else if (variable instanceof Double) {
            return Constants.TYPES.DOUBLE;
        } else if (variable instanceof Float) {
            return Constants.TYPES.FLOAT;
        } else if (variable instanceof Long) {
            return Constants.TYPES.LONG;
        } else if (variable instanceof Short) {
            return Constants.TYPES.SHORT;
        } else {
            return Constants.TYPES.UNKNOWN;
        }
    }

    /**
     * Returns the type of a variable given type name.
     * @param typeName
     * @return object type as Constants.TYPES
     */
    public static Constants.TYPES getType(String typeName) {
        if (typeName == null) {
            return null;
        }

        typeName = typeName.toLowerCase(Locale.ROOT);
        if (typeName.equals("string")) {
            return Constants.TYPES.STRING;
        } else if (typeName.equals("int")) {
            return Constants.TYPES.INTEGER;
        } else if (typeName.equals("boolean")) {
            return Constants.TYPES.BOOLEAN;
        } else if (typeName.equals("double")) {
            return Constants.TYPES.DOUBLE;
        } else if (typeName.equals("float")) {
            return Constants.TYPES.FLOAT;
        } else if (typeName.equals("long")) {
            return Constants.TYPES.LONG;
        } else if (typeName.equals("short")) {
            return Constants.TYPES.SHORT;
        } else if (typeName.equals("message")) {
            return Constants.TYPES.MESSAGE;
        } else {
            return Constants.TYPES.UNKNOWN;
        }
    }

    /**
     * Find the map that contains a variable by traversing variable stack.
     * @param carbonMessage
     * @param name
     * @return Variable value object.
     */
    public static Object getMap(CarbonMessage carbonMessage, String name) {
        Stack<Map<String, Object>> variableStack =
                (Stack<Map<String, Object>>) carbonMessage.getProperty(Constants.VARIABLE_STACK);
        return findVariable(variableStack.peek(), name, true);
    }

    /**
     * Find the value of a given key traversing the variable stack.
     * @param carbonMessage
     * @param name
     * @return Variable value object.
     */
    public static Object getVariable(CarbonMessage carbonMessage, String name) {
        Stack<Map<String, Object>> variableStack =
                (Stack<Map<String, Object>>) carbonMessage.getProperty(Constants.VARIABLE_STACK);
        return findVariable(variableStack.peek(), name, false);
    }

    /**
     * Recursively search variable stack for given key and return either the map containing the variable or
     * the variable value itself.
     * @param variables
     * @param name
     * @param map toggle whether variable value or map containing variable should be returned.
     * @return Variable value or map object.
     */
    private static Object findVariable(Map<String, Object> variables, String name, boolean map) {
        if (variables.containsKey(name)) {
            if (!map) {
                return variables.get(name);
            } else {
                return variables;
            }
        } else {
            if (variables.containsKey(Constants.GW_GT_SCOPE)) {
                return findVariable((Map<String, Object>) variables.get(Constants.GW_GT_SCOPE), name, map);
            } else {
                return null;
            }
        }
    }

    /**
     * Method is used to remove the parent map pointer from the top of the stack map, This is done to stop the
     * variable search traversal from at the given point, without going further.
     *
     * @param cMsg CarbonMessage
     */
    public static void removeParentMap(CarbonMessage cMsg) {
        // check if stack exists in cMsg,
        Stack<Map<String, Object>> variableStack;
        if (cMsg.getProperty(Constants.VARIABLE_STACK) != null) {
            variableStack = (Stack<Map<String, Object>>) cMsg.getProperty(Constants.VARIABLE_STACK);
            variableStack.peek().remove(Constants.GW_GT_SCOPE);
        }
    }

    /**
     * Validate if the both objects are in the same type which recognized in Gateway
     *
     * @param object1 first object
     * @param object2 second object
     * @return true if both are same type, false otherwise
     */
    public static boolean isBothSameType(Object object1, Object object2) {
        if ((object1 instanceof String) && (object2 instanceof String) ||
                (object1 instanceof Integer) && (object2 instanceof Integer) ||
                (object1 instanceof Boolean) && (object2 instanceof Boolean) ||
                (object1 instanceof Double) && (object2 instanceof Double) ||
                (object1 instanceof Float) && (object2 instanceof Float) ||
                (object1 instanceof Long) && (object2 instanceof Long) ||
                (object1 instanceof Short) && (object2 instanceof Short) ||
                (object1 instanceof CarbonMessage) && (object2 instanceof CarbonMessage)) {
            return true;
        }
        return false;
    }

}
