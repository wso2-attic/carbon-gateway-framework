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

package org.wso2.carbon.gateway.core.inbound;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;


/**
 * Service component for InboundEndpoint Providers.
 *
 * This will wait until all the InboundProviders are available and then register the
 * InboundProviderRegistry as a service so that others can consume it.
 */
@Component(
        name = "org.wso2.carbon.gateway.core.inbound.InboundServiceComponent",
        immediate = true,
        property = {
                "capability-name=org.wso2.carbon.gateway.core.inbound.InboundEPProvider",
                "component-key=inbound-provider"
        }
)
public class InboundEPServiceComponent implements RequiredCapabilityListener {

    private static final Logger logger = LoggerFactory.getLogger(InboundEPServiceComponent.class);

    private BundleContext bundleContext;

    private boolean isAllProviderAvailable;

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

        if (isAllProviderAvailable) {
            bundleContext.registerService(ProviderRegistry.class,
                                          InboundEPProviderRegistry.getInstance(), null);
        }
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
      if (logger.isDebugEnabled()) {
          logger.debug("All Inbound Providers available");
      }

      isAllProviderAvailable = true;

      if (bundleContext != null) {
          bundleContext.registerService(ProviderRegistry.class,
                                        InboundEPProviderRegistry.getInstance(), null);
      }
    }

    @Reference(
            name = "InboundEndpoint-Service",
            service = Provider.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeInboundProvider"
    )
    protected void addInboundProvider(Provider inboundEndpointProvider) {
        InboundEPProviderRegistry.getInstance().registerInboundEPProvider(inboundEndpointProvider);
    }

    protected void removeInboundProvider(Provider inboundEndpointProvider) {
        InboundEPProviderRegistry.getInstance().unregisterInboundEPProvider(inboundEndpointProvider);
    }

}
