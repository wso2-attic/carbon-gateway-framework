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

package org.wso2.carbon.gateway.inbounds.http;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.gateway.core.config.ConfigRegistry;
import org.wso2.carbon.gateway.core.config.ConfigRegistryObserver;
import org.wso2.carbon.gateway.core.inbound.Dispatcher;
import org.wso2.carbon.gateway.core.inbound.InboundEPDeployer;
import org.wso2.carbon.gateway.core.inbound.Provider;
import org.wso2.carbon.gateway.core.inbound.InboundEndpoint;
import org.wso2.carbon.messaging.TransportListenerManager;

/**
 * HTTP Inbound Provider
 *
 * This is responsible for providing a http inbound endpoint instance to engine
 * And also this is responsible for registering other required services
 *
 */
@Component(
        name = "org.wso2.carbon.gateway.inbounds.http.HTTPInboundEPProvider",
        immediate = true,
        service = Provider.class
)
public class HTTPInboundEPProvider implements Provider {

    @Activate
    protected void start(BundleContext bundleContext) {
        bundleContext.registerService(TransportListenerManager.class,
                                      HTTPListenerManager.getInstance(), null);
        bundleContext.registerService(ConfigRegistryObserver.class,
                                      HTTPInboundEPDispatcher.getInstance(), null);
    }

    @Override
    public String getProtocol() {
        return "http";
    }

    @Override
    public InboundEPDeployer getInboundDeployer() {
        return HTTPListenerManager.getInstance();
    }

    @Override
    public InboundEndpoint getInboundEndpoint() {
        return new HTTPInboundEP();
    }

    @Override
    public Dispatcher getInboundEndpointDispatcher() {
        return HTTPInboundEPDispatcher.getInstance();
    }

}
