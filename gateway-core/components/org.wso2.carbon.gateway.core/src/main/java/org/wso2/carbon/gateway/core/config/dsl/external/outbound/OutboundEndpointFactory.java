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
package org.wso2.carbon.gateway.core.config.dsl.external.outbound;


import org.wso2.carbon.gateway.core.outbound.OutboundEndpoint;

/**
 * Factory class to generate outbound endpoints
 */
public class OutboundEndpointFactory {
    public static OutboundEndpoint getOutboundEndpoint(OutboundEndpointType outboundEndpointType,
                                                       String name, String uri) {
        OutboundEndpoint outboundEndpoint = null;
        switch (outboundEndpointType) {
            case http:
                //outboundEndpoint = new HTTPOutboundEndpoint(name, uri);
                break;
            case jms:
                break;
            case mqtt:
                break;
            default:
                break;
        }
        return outboundEndpoint;
    }

}
