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

package org.wso2.ballerina.core.flow;

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
 * Service Component for for MediatorProviders
 *
 * This will wait until all the MediatorProviders availability and register ProviderRegistry as a
 * service so that others can consume it.
 *
 */
@Component(
        name = "org.wso2.ballerina.core.flow.MediatorServiceComponent",
        immediate = true,
        property = {
                "componentName=mediator-provider"
        }
)
public class MediatorServiceComponent implements RequiredCapabilityListener {

    private static final Logger logger = LoggerFactory.getLogger(MediatorServiceComponent.class);

    private BundleContext bundleContext;

    private boolean isAllProviderAvailable;

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

        if (isAllProviderAvailable) {
            bundleContext.registerService(ProviderRegistry.class,
                                          MediatorProviderRegistry.getInstance(), null);
        }
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        if (logger.isDebugEnabled()) {
            logger.debug("All Mediator Providers available");
        }

        isAllProviderAvailable = true;

        if (bundleContext != null) {
            bundleContext.registerService(ProviderRegistry.class,
                                          MediatorProviderRegistry.getInstance(), null);
        }
    }

    @Reference(
            name = "Mediator-Service",
            service = MediatorProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterMediatorProvider"
    )
    protected void registerMediatorProvider(MediatorProvider mediatorProvider) {
        MediatorProviderRegistry.getInstance().registerMediatorProvider(mediatorProvider);
    }

    protected void unregisterMediatorProvider(MediatorProvider mediatorProvider) {
        MediatorProviderRegistry.getInstance().unregisterMediatorProvider(mediatorProvider);
    }
}
