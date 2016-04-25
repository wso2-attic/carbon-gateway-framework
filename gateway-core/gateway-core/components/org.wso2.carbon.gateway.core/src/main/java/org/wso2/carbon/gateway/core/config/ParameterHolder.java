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

package org.wso2.carbon.gateway.core.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Holder for set of parameters
 */
public class ParameterHolder {

    Map<String, Parameter> parameters  = new HashMap<>();

    public void addParameter(Parameter parameter) {
        parameters.put(parameter.getName(), parameter);
    }

    public void removeParameter(String paramName) {
        parameters.remove(paramName);
    }

    public Parameter getParameter(String paramName) {
        return parameters.get(paramName);
    }

    public Map getParameters() {
        return parameters;
    }

}
