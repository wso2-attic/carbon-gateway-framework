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
package org.wso2.carbon.gateway.core.config.dsl.external.inbound;

import org.wso2.carbon.gateway.core.inbound.InboundEPProviderRegistry;
import org.wso2.carbon.gateway.core.inbound.InboundEndpoint;

/**
 * Factory class to generate inbound endpoints
 */
public class InboundEndpointFactory {
    public static InboundEndpoint getInboundEndpoint(InboundEndpointType inboundEndpointType, String name, int port) {
        InboundEndpoint inboundEndpoint = null;
        switch (inboundEndpointType) {
            case http:
              //  inboundEndpoint = new HTTPInboundEP(name, port);
                inboundEndpoint = InboundEPProviderRegistry.getInstance().getProvider("http").getInboundEndpoint();
                inboundEndpoint.setName(name);

                break;
            case jms:
                break;
            case mqtt:
                break;
            default:
                break;
        }
        return inboundEndpoint;
    }

}
