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

    private Scope scope;

    private String key;

    private String value;

    public Source(String key, Scope scope) {
        this.scope = scope;
        this.key = key;
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
}
