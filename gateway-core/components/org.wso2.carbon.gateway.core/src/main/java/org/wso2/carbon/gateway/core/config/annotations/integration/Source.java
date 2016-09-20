/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.gateway.core.config.annotations.integration;

import org.wso2.carbon.gateway.core.config.ConfigConstants;
import org.wso2.carbon.gateway.core.config.annotations.Annotation;

import java.util.Map;

/**
 * Integration level annotation @Source used to specify the source parameters.
 */
public class Source extends Annotation<Map<String, String>> {

    public Source() {
        super(ConfigConstants.AN_SOURCE);
    }
    public Source(Map<String, String> values) {
        super(ConfigConstants.AN_SOURCE, values);
    }

    public String getHost() {
        return getValue().get("host");
    }

    public String getPort() {
        return getValue().get("port");
    }

    public String getProtocol() {
        return getValue().get("protocol");
    }
}
