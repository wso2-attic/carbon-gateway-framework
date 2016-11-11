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

package org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter;

/**
 * A class that represents the source condition of the message to be evaluated.
 */
public class Source {

    /**
     * This is, as the the variable name itself suggests, the scope of the message
     * being evaluated. eg: $header, $body
     */
    private Scope scope;

    /**
     * This is what we evaluate the message against.
     * eg: In case of content based routing, given the eval expression "$body:/quote/text()[1]"
     * The key would be "/quote/text()[1]"
     */
    private String key;

    /**
     * This is the part of the key without the scope value.
     * i.e. given the eval expression "$body:/quote/text()[1]" the value would be /quote/text()[1]
     */
    private String value;

    /**
     * This is the path language eg: XPath in case of content based routing.
     */
    private String pathLanguage;

    public Source(String key, Scope scope) {
        this.scope = scope;
        this.key = key;
    }

    /**
     * This is the constructor to be used when an XPath/JSONPath based eval expression is encountered.
     *
     * @param value This corresponds to the "value" variable.
     * @param pathLanguage This corresponds to the "pathLanguage" variable
     */
    public Source(String value, String pathLanguage) {
        this.value = value;
        this.pathLanguage = pathLanguage;
        if (this.value.contains("$body")) {
            key = this.value.substring(this.value.indexOf(":") + 1);
            scope = Scope.BODY;
        }
    }

    public Source(String value) {
        this.value = value;
        if (this.value.contains("$header")) {
            key = this.value.substring(this.value.indexOf(".") + 1);
            scope = Scope.HEADER;
        }

    }

    public Scope getScope() {
        return scope;
    }

    public String getKey() {
        return key;
    }

    public String getPathLanguage() {
        return pathLanguage;
    }
}
