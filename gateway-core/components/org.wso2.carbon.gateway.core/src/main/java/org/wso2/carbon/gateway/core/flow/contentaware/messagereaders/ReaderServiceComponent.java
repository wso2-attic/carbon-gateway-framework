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

package org.wso2.carbon.gateway.core.flow.contentaware.messagereaders;

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
        name = "org.wso2.carbon.gateway.core.flow.contentaware.messagereaders.ReaderServiceComponent",
        immediate = true,
        property = {
                "componentName=reader-provider"
        })
public class ReaderServiceComponent implements RequiredCapabilityListener {

    private static final Logger logger = LoggerFactory.getLogger(ReaderServiceComponent.class);

    private BundleContext bundleContext;

    private boolean isAllProviderAvailable;

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

        if (isAllProviderAvailable) {
            bundleContext.registerService(ReaderProvider.class, ReaderProviderRegistry.getInstance(), null);
        }
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        if (logger.isDebugEnabled()) {
            logger.debug("All Mediator Providers available");
        }

        isAllProviderAvailable = true;
        if (bundleContext != null) {
            bundleContext.registerService(ReaderProvider.class, ReaderProviderRegistry.getInstance(), null);
        }
    }

    @Reference(
            name = "Reader-Service",
            service = Reader.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterReader")
    protected void registerReader(Reader readerProvider) {
        ReaderProviderRegistry.getInstance().registerBuilder(readerProvider.getContentType(), readerProvider);
    }

    protected void unregisterReader(Reader readerProvider) {
        ReaderProviderRegistry.getInstance().unregisterBuilder(readerProvider.getContentType(), readerProvider);
    }
}
