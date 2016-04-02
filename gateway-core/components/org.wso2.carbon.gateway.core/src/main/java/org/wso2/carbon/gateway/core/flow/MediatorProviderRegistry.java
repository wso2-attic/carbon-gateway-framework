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

package org.wso2.carbon.gateway.core.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.FilterMediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.invokers.CallMediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.invokers.RespondMediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.manipulators.EnrichMediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.manipulators.LogMediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.manipulators.TransformMediator;

import java.util.HashMap;
import java.util.Map;

/**
 * Store for MediatorProviders
 */
public class MediatorProviderRegistry implements ProviderRegistry {

    private Map<String, MediatorProvider> mediatorProviders = new HashMap<>();

    private Map<String, Class> builtinMediators = new HashMap<>();

    private static MediatorProviderRegistry instance = new MediatorProviderRegistry();

    private static final Logger log = LoggerFactory.getLogger(MediatorProviderRegistry.class);

    private final Class[] builtinMediatorClasses = {
            CallMediator.class,
            FilterMediator.class,
            RespondMediator.class,
            LogMediator.class,
            EnrichMediator.class,
            TransformMediator.class
    };

    private MediatorProviderRegistry() {
        loadMediators();
    }

    public static MediatorProviderRegistry getInstance() {
        return instance;
    }

    public void registerMediatorProvider(MediatorProvider mediatorProvider) {
        mediatorProviders.put(mediatorProvider.getName(), mediatorProvider);
    }

    private void loadMediators() {
        for (Class c : builtinMediatorClasses) {
            try {
                Mediator med = (Mediator) c.newInstance();
                builtinMediators.put(med.getName(), c);
            } catch (InstantiationException e) {
                log.error("Error while instantiation of mediator " + c.getName(), e);
            } catch (IllegalAccessException e) {
                log.error("Illegal Access error while instantiation of " + c.getName(), e);
            }
        }
    }

    public void unregisterMediatorProvider(MediatorProvider mediatorProvider) {
        mediatorProviders.remove(mediatorProvider.getName());
    }

    public void unregisterMediatorProvider(String mediatorName) {
        mediatorProviders.remove(mediatorName);
    }

    public Mediator getMediator(String name) {

        Class c = builtinMediators.get(name);

        if (c != null) {
            try {
                return (Mediator) c.newInstance();
            } catch (InstantiationException e) {
                log.error("Error while instantiation of " + name, e);
            } catch (IllegalAccessException e) {
                log.error("Illegal Access error while instantiation of " + name, e);
            }

        } else if (mediatorProviders.containsKey(name)) {
            return mediatorProviders.get(name).getMediator();
        } else {
            log.error("Mediator implementation not found for " + name);
        }

        return null;
    }

}
