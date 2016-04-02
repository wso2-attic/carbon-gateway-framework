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

package org.wso2.carbon.gateway.core.config;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Service component for Config Registry.
 *
 * This service is responsible for getting all the Config Registry observers
 * subscribed to the Config Registry
 */
@Component(
        name = "ConfigRegistryServiceComponent",
        immediate = true
)
public class ConfigRegistryServiceComponent {

    @Reference(
            name = "ConfigRegistry-Observer-Service",
            service = ConfigRegistryObserver.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeConfigRegistryObserver"
    )
    protected void addConfigRegistryObserver(ConfigRegistryObserver observer) {
        ConfigRegistry.getInstance().registerObserver(observer);
    }

    protected void removeConfigRegistryObserver(ConfigRegistryObserver observer) {
        ConfigRegistry.getInstance().unregisterObserver(observer);
    }

}
