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

package org.wso2.carbon.gateway.core.outbound;

import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Outbound Endpoint Interface
 */
public interface OutboundEndpoint {

    int getTimeOut();

    void setTimeOut(int timeOut);

    String getName();

    boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback)
            throws Exception;

    void setParameters(ParameterHolder parameters);

    void setName(String name);

    void setUri(String uri);

    String getUri();

}
