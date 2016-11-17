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
package org.wso2.ballerina.inbounds.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.config.ConfigRegistryObserver;
import org.wso2.ballerina.core.inbound.Dispatcher;
import org.wso2.ballerina.core.inbound.InboundEndpoint;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This handles the message dispatching for HTTP Inbound Endpoints
 */
public class HTTPInboundEPDispatcher implements Dispatcher, ConfigRegistryObserver {

    private static HTTPInboundEPDispatcher instance = new HTTPInboundEPDispatcher();

    private static final Logger log = LoggerFactory.getLogger(HTTPInboundEPDispatcher.class);

    private HashMap<String, ArrayList<HTTPInboundEP>> httpEPRegistry;

    public static HTTPInboundEPDispatcher getInstance() {
        return instance;
    }

    private HTTPInboundEPDispatcher() {
        httpEPRegistry = new HashMap<>();
    }

    @Override
    public boolean dispatch(CarbonMessage cMsg, CarbonCallback callback) {

        String interfaceId = (String) cMsg.getProperty(Constants.LISTENER_INTERFACE_ID);

        ArrayList<HTTPInboundEP> endpointsOnPort = httpEPRegistry.get(interfaceId);
        if (endpointsOnPort == null) {
            log.error("No endpoint found for interface id : " + interfaceId);
            return false;
        }

        boolean foundMatchingEndpoint = false;

        for (HTTPInboundEP endpoint : endpointsOnPort) {
            if (endpoint.canReceive(cMsg)) {
                foundMatchingEndpoint = endpoint.receive(cMsg, callback);
                break;
            }
        }

        if (!foundMatchingEndpoint) {
            callback.done(createErrorMessage("Cannot find a matching Resource", 404));
        }

        return false;
    }

    @Override
    public String getProtocol() {
        return "http";
    }

    @Override
    public void endpointAdded(InboundEndpoint endpoint) {
        if (!(endpoint instanceof HTTPInboundEP)) { //If not an HTTPInboundEP just skip
            return;
        }

        HTTPInboundEP httpInboundEP = (HTTPInboundEP) endpoint;

        String interfaceId = httpInboundEP.getInterfaceId();
        ArrayList<HTTPInboundEP> endpointsForAPort = httpEPRegistry.get(interfaceId);

        if (endpointsForAPort == null) {
            endpointsForAPort = new ArrayList<>();
            httpEPRegistry.put(interfaceId, endpointsForAPort);
        }
        endpointsForAPort.add(httpInboundEP);
    }

    @Override
    public void endpointRemoved(InboundEndpoint endpoint) {
        if (!(endpoint instanceof HTTPInboundEP)) { //If not an HTTPInboundEP just skip
            return;
        }

        HTTPInboundEP httpInboundEP = (HTTPInboundEP) endpoint;

        String interfaceId = httpInboundEP.getInterfaceId();
        ArrayList<HTTPInboundEP> endpointsForAPort = httpEPRegistry.get(interfaceId);
        if (endpointsForAPort != null) {
            endpointsForAPort.remove(httpInboundEP);
            if (endpointsForAPort.isEmpty()) {
                httpEPRegistry.remove(interfaceId);
            }
        }

    }

    private CarbonMessage createErrorMessage(String payload, int statusCode) {
        DefaultCarbonMessage response = new DefaultCarbonMessage();

        response.setStringMessageBody(payload);
        byte[] errorMessageBytes = payload.getBytes(Charset.defaultCharset());

        Map<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_CONNECTION,
                org.wso2.carbon.transport.http.netty.common.Constants.KEEP_ALIVE);
        transportHeaders.put(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_CONTENT_ENCODING,
                org.wso2.carbon.transport.http.netty.common.Constants.GZIP);
        transportHeaders.put(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_CONTENT_TYPE,
                org.wso2.carbon.transport.http.netty.common.Constants.TEXT_PLAIN);
        transportHeaders.put(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_CONTENT_LENGTH,
                (String.valueOf(errorMessageBytes.length)));

        response.setHeaders(transportHeaders);

        response.setProperty(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_STATUS_CODE, statusCode);
        response.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        return response;

    }
}
