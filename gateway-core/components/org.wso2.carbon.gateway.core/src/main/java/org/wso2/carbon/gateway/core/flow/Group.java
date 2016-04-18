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

package org.wso2.carbon.gateway.core.flow;

import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Representation for a Service Group
 */
public class Group {

    private String path;
    private String method;
    private String pipeline;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public boolean canProcess(CarbonMessage cMsg) {
        String method = (String) cMsg.getProperty(Constants.SERVICE_METHOD);
        String subGroupPAth = (String) cMsg.getProperty(Constants.SERVICE_SUB_GROUP_PATH);

        if (this.method != null && !method.matches(this.method)) { //method is optional
            return false;
        }

        if (!subGroupPAth.startsWith(path)) {
            return false;
        }

        return true;
    }

}
