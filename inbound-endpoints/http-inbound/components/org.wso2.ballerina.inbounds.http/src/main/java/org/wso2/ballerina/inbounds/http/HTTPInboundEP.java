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
import org.wso2.ballerina.core.config.Parameter;
import org.wso2.ballerina.core.config.ParameterHolder;
import org.wso2.ballerina.core.inbound.InboundEndpoint;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;

/**
 * HTTP Inbound Endpoint
 */
public class HTTPInboundEP extends InboundEndpoint {

    private static final Logger log = LoggerFactory.getLogger(InboundEndpoint.class);

    private String context;

    private String interfaceId;

    public HTTPInboundEP() {
    }

    public HTTPInboundEP(String name, String interfaceId) {
        setName(name);
        setInterfaceId(interfaceId);

    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public boolean canReceive(CarbonMessage cMsg) {

        String uri = (String) cMsg.getProperty(Constants.TO);
        return uri.startsWith(context);
    }

    public boolean receive(CarbonMessage cMsg, CarbonCallback callback) {
        if (log.isDebugEnabled()) {
            log.debug("HTTP Endpoint : " + getName() + " received the message");
        }
        cMsg.setProperty(org.wso2.ballerina.core.Constants.SERVICE_CONTEXT, context);

        String uri = (String) cMsg.getProperty(Constants.TO);
        String[] uriSplit = uri.split(context);
        String subPath = "";
        if (uriSplit.length > 1) {
            subPath = uriSplit[1];
        }
        cMsg.setProperty(org.wso2.ballerina.core.Constants.SERVICE_SUB_GROUP_PATH, subPath);

        cMsg.setProperty(org.wso2.ballerina.core.Constants.SERVICE_METHOD, cMsg.getProperty("HTTP_METHOD"));
        return  super.receive(cMsg, callback);

    }

    @Override
    public String getProtocol() {
        return "http";
    }

    @Override
    public void setParameters(ParameterHolder parameters) {

        context = parameters.getParameter(org.wso2.ballerina.core.Constants.CONTEXT).getValue();
        Parameter interfaceParam = parameters.getParameter(org.wso2.ballerina.core.Constants.INTERFACE);
        interfaceId = interfaceParam.getValue();
        if (interfaceId == null) {
            log.error("interface cannot be null");
        }

    }

    @Override
    public String getName() {
        return " bounded interface :- " + interfaceId + " context :- " + context;
    }
}
