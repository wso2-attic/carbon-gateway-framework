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
package org.wso2.carbon.gateway.mediators.datamapper.engine.core.models;

/**
 * This class implements {@link Model} interface to hold String data.
 */
public class StringModel implements Model<String> {

    private String mapDataHolder;

    public StringModel(String model) {
        mapDataHolder = model;
    }

    @Override
    public String getModel() {
        return mapDataHolder;
    }

    @Override
    public void setModel(String model) {
        mapDataHolder = model;
    }
}
