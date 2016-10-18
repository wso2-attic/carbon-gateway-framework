/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.gateway.mediators.datamapper.engine.input.readers.events;

public enum ReaderEventType {

    OBJECT_START("ObjectStart"),
    OBJECT_END("ObjectEnd"),
    ARRAY_START("ArrayStart"),
    ARRAY_END("ArrayEnd"),
    FIELD("Field"),
    TERMINATE("Terminate"),
    ANONYMOUS_OBJECT_START("AnonymousObjectStart"),
    PRIMITIVE("Primitive");
    private final String value;

    ReaderEventType(String value) {
        this.value = value;
    }

    @Override public String toString() {
        return value;
    }

}

