/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.inbound.Dispatcher;
import org.wso2.ballerina.core.inbound.InboundEPProviderRegistry;
import org.wso2.ballerina.core.inbound.Provider;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Locale;
import java.util.Map;
import java.util.Stack;

/**
 * A Util class which is used by Thread Initiated classes.
 */
public class WorkerUtil {

    private static final Logger logger = LoggerFactory.getLogger(WorkerUtil.class);

    /**
     * Select the InboundEndpoint according to protocol and switchDisruptor
     * @param carbonMessage
     * @param carbonCallback
     */
    public static void dispatchToInboundEndpoint(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {

        Provider provider = null;
        carbonMessage.setProperty(org.wso2.ballerina.core.Constants.VARIABLE_STACK, new Stack<Map<String, Object>>());

        String protocol = (String) carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.PROTOCOL);
        if (null != protocol) {
            provider = InboundEPProviderRegistry.getInstance().getProvider(protocol.toLowerCase(Locale.getDefault()));
        }

        if (provider == null) {
            logger.error("Cannot handle protocol : " + protocol + " , Provider not found");
            return;
        }
        // Decide the dispatcher
        Dispatcher dispatcher = provider.getInboundEndpointDispatcher();
        if (dispatcher == null) {
            logger.error("Cannot handle protocol : " + protocol + " , Dispatcher not found");
            return;
        }
        dispatcher.dispatch(carbonMessage, carbonCallback);
    }
}
