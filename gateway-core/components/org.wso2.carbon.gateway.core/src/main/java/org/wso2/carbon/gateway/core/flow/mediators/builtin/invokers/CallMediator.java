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

package org.wso2.carbon.gateway.core.flow.mediators.builtin.invokers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.config.ConfigRegistry;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.AbstractMediator;
import org.wso2.carbon.gateway.core.flow.FlowControllerMediateCallback;
import org.wso2.carbon.gateway.core.flow.Invoker;
import org.wso2.carbon.gateway.core.outbound.OutboundEndpoint;
import org.wso2.carbon.gateway.core.util.VariableUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Send a Message out from Pipeline to an Outbound Endpoint
 */
public class CallMediator extends AbstractMediator implements Invoker {


    private String outboundEPKey;

    private OutboundEndpoint outboundEndpoint;

    private static final Logger log = LoggerFactory.getLogger(CallMediator.class);

    public CallMediator() {
    }

    public CallMediator(String outboundEPKey) {
        this.outboundEPKey = outboundEPKey;
    }

    public CallMediator(OutboundEndpoint outboundEndpoint) {
        this.outboundEndpoint = outboundEndpoint;
    }

    public void setParameters(ParameterHolder parameterHolder) {
        outboundEPKey = parameterHolder.getParameter("endpointKey").getValue();
    }

    @Override
    public String getName() {
        return "call";
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback)
            throws Exception {

        OutboundEndpoint endpoint = outboundEndpoint;
        if (endpoint == null) {
            endpoint = ConfigRegistry.getInstance().getOutboundEndpoint(outboundEPKey);

            if (endpoint == null) {
                log.error("Outbound Endpoint : " + outboundEPKey + "not found ");
                return false;
            }
        }

        CarbonCallback callback = new FlowControllerMediateCallback(carbonCallback, this,
                VariableUtil.getVariableStack(carbonMessage));

        endpoint.receive(carbonMessage, callback);
        return false;
    }


}
