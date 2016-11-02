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

package org.wso2.carbon.gateway.core.service;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.MessageProcessor;
import org.wso2.carbon.gateway.core.ServiceContextHolder;
import org.wso2.carbon.gateway.core.worker.config.ThreadModelConfiguration;
import org.wso2.carbon.gateway.core.worker.config.YAMLEngineConfigurationBuilder;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportSender;


/**
 * Service component for Gateway.
 */
@Component(
        name = "org.wso2.carbon.gateway.core.service.GatewayServiceComponent",
        immediate = true
)
public class GatewayServiceComponent {

    private static final Logger log = LoggerFactory.getLogger(GatewayServiceComponent.class);

    @Activate
    protected void start(BundleContext bundleContext) {
        try {
            log.info("Starting WSO2 Integration...!");

            //Creating the processor and registering the service
            bundleContext.registerService(CarbonMessageProcessor.class, new MessageProcessor(), null);
            ThreadModelConfiguration threadModelConfiguration = YAMLEngineConfigurationBuilder.build();
            threadModelConfiguration.configure();
        } catch (Exception ex) {
            String msg = "Error while loading WSO2 Integration";
            log.error(msg, ex);
            throw new RuntimeException(msg, ex);
        }
    }

    @Reference(
               name = "transport-sender",
               service = TransportSender.class,
               cardinality = ReferenceCardinality.OPTIONAL,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "removeTransportSender"
    )
    protected void addTransportSender(TransportSender transportSender) {
        ServiceContextHolder.getInstance().addTransportSender(transportSender);
    }

    protected void removeTransportSender(TransportSender transportSender) {
        ServiceContextHolder.getInstance().removeTransportSender(transportSender);
    }


}
