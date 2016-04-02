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
package org.wso2.carbon.gateway.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.inbound.Dispatcher;
import org.wso2.carbon.gateway.core.inbound.Provider;
import org.wso2.carbon.gateway.core.inbound.InboundEPProviderRegistry;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportSender;

/**
 *  Message Processor Implementation for Gateway
 *
 */
public class MessageProcessor implements CarbonMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);

    @Override
    public boolean receive(CarbonMessage cMsg, CarbonCallback callback) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Gateway received a message");
        }

        String protocol = "http";  //TODO: Take from cMsg

        Provider provider = InboundEPProviderRegistry.getInstance().getProvider(protocol);

        if (provider == null) {
            log.error("Cannot handle protocol : " + protocol + " , Provider not found");
            return false;
        }

        // Decide the dispatcher
        Dispatcher dispatcher = provider.getInboundEndpointDispatcher();
        if (dispatcher == null) {
            log.error("Cannot handle protocol : " + protocol + " , Dispatcher not found");
            return false;
        }

        dispatcher.dispatch(cMsg, callback);
        return false;
    }

    @Override
    public void setTransportSender(TransportSender sender) {

    }

    @Override
    public String getId() {
        return null;
    }
}
