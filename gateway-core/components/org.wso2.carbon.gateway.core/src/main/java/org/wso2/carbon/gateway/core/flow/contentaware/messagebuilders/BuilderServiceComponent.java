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

package org.wso2.carbon.gateway.core.flow.contentaware.messagebuilders;



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
 * A service class for register unregister builders
 * This waits until all the builders get registered.
 */
@Component(
        name = "org.wso2.carbon.gateway.core.flow.contentaware.messagebuilders.BuilderServiceComponent",
        immediate = true,
        property = {
                "componentName=builder-provider"
        })
public class BuilderServiceComponent implements RequiredCapabilityListener {

    private static final Logger logger = LoggerFactory.getLogger(BuilderServiceComponent.class);

    private BundleContext bundleContext;

    private boolean isAllProviderAvailable;

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

        if (isAllProviderAvailable) {
            bundleContext.registerService(BuilderProvider.class, BuilderProviderRegistry.getInstance(), null);
        }
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        if (logger.isDebugEnabled()) {
            logger.debug("All Mediator Providers available");
        }

        isAllProviderAvailable = true;

        if (bundleContext != null) {
            bundleContext.registerService(BuilderProvider.class, BuilderProviderRegistry.getInstance(), null);
        }
    }

    @Reference(
            name = "Builder-Service",
            service = Builder.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterBuilder")
    protected void registerBuilder(Builder mediatorProvider) {
        BuilderProviderRegistry.getInstance().registerBuilder(mediatorProvider.getContentType(), mediatorProvider);
    }

    protected void unregisterBuilder(Builder mediatorProvider) {
        BuilderProviderRegistry.getInstance().unregisterBuilder(mediatorProvider.getContentType(), mediatorProvider);
    }
}
