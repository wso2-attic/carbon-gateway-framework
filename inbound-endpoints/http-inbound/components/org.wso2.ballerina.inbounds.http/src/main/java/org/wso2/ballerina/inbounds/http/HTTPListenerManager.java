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

package org.wso2.ballerina.inbounds.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.inbound.InboundEPDeployer;
import org.wso2.ballerina.core.inbound.InboundEndpoint;
import org.wso2.carbon.messaging.TransportListener;
import org.wso2.carbon.messaging.TransportListenerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An InboundEndpoint Manager class
 * <p/>
 * This acts as the Deployer for HTTP Inbound Endpoint as well as a Transport Listener Manager
 */
public class HTTPListenerManager implements TransportListenerManager, InboundEPDeployer {

    private volatile TransportListener transportListener;

    private Map<String, InboundEndpoint> earlyInbounds = new ConcurrentHashMap<>();

    private Map<String, List<HTTPInboundEP>> deployedInbounds = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(HTTPListenerManager.class);

    private static HTTPListenerManager inboundEndpointDeployer = new HTTPListenerManager();

    public static HTTPListenerManager getInstance() {
        return inboundEndpointDeployer;
    }

    private HTTPListenerManager() {
    }

    @Override
    public TransportListener getTransportListener() {
        return transportListener;
    }

    @Override
    public void registerTransportListener(TransportListener transportListener) {
        this.transportListener = transportListener;
        for (Map.Entry entry : earlyInbounds.entrySet()) {
            deploy((InboundEndpoint) entry.getValue());
            earlyInbounds.remove(entry.getKey());
        }
    }

    /**
     * Deploy inbound endpoint in transport level by opening up ports
     * @param inboundEndpoint
     */
    public void deploy(InboundEndpoint inboundEndpoint) {
        if (transportListener == null) {
            earlyInbounds.put(inboundEndpoint.getName(), inboundEndpoint);
            return;
        }
        String interfaceId = ((HTTPInboundEP) inboundEndpoint).getInterfaceId();
        List<HTTPInboundEP> inboundEndpointList = deployedInbounds.get(interfaceId);
        if (inboundEndpointList == null) {
            List<HTTPInboundEP> endpointList = new ArrayList<>();
            endpointList.add((HTTPInboundEP) inboundEndpoint);
            deployedInbounds.put(interfaceId, endpointList);
            transportListener.bind(interfaceId);
        } else if (inboundEndpointList.isEmpty()) {
            transportListener.bind(interfaceId);
            inboundEndpointList.add((HTTPInboundEP) inboundEndpoint);
        }

    }

    /**
     * Closing ports and undeploy inbound from transport level
     * @param inboundEndpoint
     */
    public void undeploy(InboundEndpoint inboundEndpoint) {
        String interfaceId = ((HTTPInboundEP) inboundEndpoint).getInterfaceId();
        List<HTTPInboundEP> endpointList = deployedInbounds.get(interfaceId);
        endpointList.remove(inboundEndpoint);
        if (endpointList.isEmpty()) {
            transportListener.unBind(interfaceId);
        }

    }
}
