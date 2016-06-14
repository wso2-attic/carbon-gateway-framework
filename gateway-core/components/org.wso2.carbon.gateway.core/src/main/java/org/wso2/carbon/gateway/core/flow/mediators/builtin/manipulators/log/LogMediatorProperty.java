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

package org.wso2.carbon.gateway.core.flow.mediators.builtin.manipulators.log;

import java.util.Map;

/**
 * A class for represent property values within log
 */
public class LogMediatorProperty {

    private String key;
    private String value;
    private String expression;
    private Map<String, String> nameSpaceMap;

    LogMediatorProperty(String key, String value, String expression, Map<String, String> nameSpaceMap) {
        this.key = key;
        this.value = value;
        this.expression = expression;
        this.nameSpaceMap = nameSpaceMap;
    }

    LogMediatorProperty(String key, String value, String expression) {
        this.key = key;
        this.value = value;
        this.expression = expression;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public String getExpression() {
        return expression;
    }

    public Map<String, String> getNameSpaceMap() {
        return nameSpaceMap;
    }
}
