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
import org.wso2.carbon.gateway.core.config.IntegrationConfigRegistry;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.AbstractMediator;
import org.wso2.carbon.gateway.core.flow.FlowControllerMediateCallback;
import org.wso2.carbon.gateway.core.flow.Invoker;
import org.wso2.carbon.gateway.core.flow.MediatorType;
import org.wso2.carbon.gateway.core.outbound.OutboundEndpoint;
import org.wso2.carbon.gateway.core.util.VariableUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;

import static org.wso2.carbon.gateway.core.Constants.ENDPOINT_KEY;
import static org.wso2.carbon.gateway.core.Constants.INTEGRATION_KEY;
import static org.wso2.carbon.gateway.core.Constants.MESSAGE_KEY;
import static org.wso2.carbon.gateway.core.Constants.RETURN_VALUE;

/**
 * Send a Message out from Pipeline to an Outbound Endpoint
 */
public class CallMediator extends AbstractMediator implements Invoker {

    private static final Logger log = LoggerFactory.getLogger(CallMediator.class);
    private String outboundEPKey;
    private String integrationKey;
    private String messageKey;
    private OutboundEndpoint outboundEndpoint;

    public CallMediator() {
    }

    public CallMediator(String outboundEPKey) {
        this.outboundEPKey = outboundEPKey;
    }

    public CallMediator(OutboundEndpoint outboundEndpoint) {
        this.outboundEndpoint = outboundEndpoint;
    }

    public void setParameters(ParameterHolder parameterHolder) {
        if (parameterHolder.getParameter(ENDPOINT_KEY) != null) {
            outboundEPKey = parameterHolder.getParameter(ENDPOINT_KEY).getValue();
        } else {
            log.error(ENDPOINT_KEY + " is not set in the configuration.");
        }
        if (parameterHolder.getParameter(MESSAGE_KEY) != null) {
            messageKey = parameterHolder.getParameter(MESSAGE_KEY).getValue();
        } else {
            log.error(MESSAGE_KEY + " is not set in the configuration.");
        }
        if (parameterHolder.getParameter(RETURN_VALUE) != null) {
            returnedOutput = parameterHolder.getParameter(RETURN_VALUE).getValue();
        } else {
            log.error(RETURN_VALUE + " is not set in the configuration.");
        }
        integrationKey = parameterHolder.getParameter(INTEGRATION_KEY).getValue();
    }

    @Override
    public MediatorType getMediatorType() {
        return MediatorType.CPU_BOUND;
    }

    @Override
    public String getName() {
        return "call";
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {

        OutboundEndpoint endpoint = outboundEndpoint;
        if (endpoint == null) {
            endpoint = IntegrationConfigRegistry.getInstance()
                    .getIntegrationConfig(integrationKey).getOutbound(outboundEPKey);

            if (endpoint == null) {
                log.error("Outbound Endpoint : " + outboundEPKey + " not found ");
                return false;
            }
        }

        //prepare CarbonMessage if it is a response message from a previous invoke
        //If the DIRECTION property of the carbonMessage is DIRECTION_RESPONSE, we can assume service chaining
        if (carbonMessage.getProperty(Constants.DIRECTION) != null &&
                            carbonMessage.getProperty(Constants.DIRECTION).equals(Constants.DIRECTION_RESPONSE)) {
            //remove Direction property
            carbonMessage.removeProperty(Constants.DIRECTION);
            //remove HTTP status code
            carbonMessage.removeProperty(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_STATUS_CODE);
            //TODO decide and remove/add any other properties (removed above to enable service chaining support)
        }

        // Retrieve the referenced message from the variable stack
        // If the retrieved message is null, skip the Call Mediator
        CarbonMessage cMsg = (CarbonMessage) getObjectFromContext(carbonMessage, messageKey);
        if (cMsg != null) {
            CarbonCallback callback = new FlowControllerMediateCallback(carbonCallback, this,
                    VariableUtil.getVariableStack(carbonMessage));
            endpoint.receive(cMsg, callback);
        } else {
            log.error("Message with identifier: " + messageKey + ", not found in this context or value is null.");
            return next(carbonMessage, carbonCallback);
        }
        return false;
    }

}
