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

package org.wso2.carbon.gateway.core.config.dsl.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.inbound.ProviderRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Service component for Gateway.
 */
@Component(
        name = "org.wso2.carbon.gateway.core.config.dsl.internal.DSLLoader",
        immediate = true
)
public class DSLLoader {


    private List<JavaConfigurationBuilder> earlyDSLs = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(DSLLoader.class);

    private boolean isReady;


    @Activate
    protected void activate(BundleContext bundleContext) {
        isReady = true;
        earlyDSLs.forEach(this::loadDSL);
    }


    @Reference(
            name = "java-dsl",
            service = JavaConfigurationBuilder.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeJavaDSL"
    )
    protected void addJavaDSL(JavaConfigurationBuilder dsl) {
        if (isReady) {
            loadDSL(dsl);
        } else {
            earlyDSLs.add(dsl);
        }
    }

    protected void removeJavaDSL(JavaConfigurationBuilder dsl) {
    }


    @Reference(
            name = "inbound-provider-registry-service",
            service = ProviderRegistry.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeInboundProviderRegistry"
    )
    protected void addInboundProviderRegistry(ProviderRegistry registry) {
    }

    protected void removeInboundProviderRegistry(ProviderRegistry registry) {
    }


    @Reference(
            name = "outbound-provider-registry-service",
            service = org.wso2.carbon.gateway.core.outbound.ProviderRegistry.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeOutboundProviderRegistry"
    )
    protected void addOutboundProviderRegistry(org.wso2.carbon.gateway.core.outbound.ProviderRegistry registry) {
    }

    protected void removeOutboundProviderRegistry(org.wso2.carbon.gateway.core.outbound.ProviderRegistry registry) {
    }

    @Reference(
            name = "mediator-provider-registry-service",
            service = org.wso2.carbon.gateway.core.flow.ProviderRegistry.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeMediatorProviderRegistry"
    )
    protected void addMediatorProviderRegistry(org.wso2.carbon.gateway.core.flow.ProviderRegistry registry) {
    }

    protected void removeMediatorProviderRegistry(org.wso2.carbon.gateway.core.flow.ProviderRegistry registry) {
    }


    /**
     * Load Java DSL configuration file
     *
     * @param javaConfigurationBuilder Java DSL
     */
    private void loadDSL(JavaConfigurationBuilder javaConfigurationBuilder) {
        if (log.isDebugEnabled()) {
            log.debug("Loading Type 1 Java DSL ..");
        }
        // TODO: Need to update the code with new IntegrationConfigRegistry object
        // Call the DSL
//        GWConfigHolder configHolder =
//                javaConfigurationBuilder.configure().getConfigHolder();

        // Register the configuration
//        ConfigRegistry.getInstance().addGWConfig(configHolder);

    }

}
