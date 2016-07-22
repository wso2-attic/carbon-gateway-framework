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

package org.wso2.carbon.gateway.core.config;


import org.wso2.carbon.gateway.core.flow.Resource;
import org.wso2.carbon.gateway.core.flow.contentaware.BaseTypeConverterRegistry;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.TypeConverterRegistry;
import org.wso2.carbon.gateway.core.inbound.InboundEPDeployer;
import org.wso2.carbon.gateway.core.inbound.InboundEPProviderRegistry;
import org.wso2.carbon.gateway.core.inbound.InboundEndpoint;
import org.wso2.carbon.gateway.core.outbound.OutboundEndpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the central place where all the configurations are stored at the runtime
 */
public class IntegrationConfigRegistry {


    private static IntegrationConfigRegistry configRegistry = new IntegrationConfigRegistry();

    private Map<String, InboundEndpoint> inboundEndpoints = new HashMap<>();

    private Map<String, Resource> resources = new HashMap<>();

    private Map<String, OutboundEndpoint> outBoundEndpointMap = new HashMap<>();

    private List<ConfigRegistryObserver> observers = new ArrayList<>();

    private Map<String, Integration> configurations = new HashMap<>();

    public static IntegrationConfigRegistry getInstance() {
        return configRegistry;
    }

    private IntegrationConfigRegistry() {
    }

    /**
     * Add an Integration Configuration to the Registry
     *
     * @param integration an Integration object
     */
    public void addIntegrationConfig(Integration integration) {
        configurations.put(integration.getName(), integration);
        updateArtifacts(integration);
    }

    /**
     * Remove a Gateway Artifact configuration
     *
     * @param configHolder a Gateway Artifact
     */
    public void removeIntegrationConfig(Integration configHolder) {
        configurations.remove(configHolder.getName());
        unDeployArtifacts(configHolder);
    }

    public Integration getIntegrationConfig(String name) {
        return configurations.get(name);
    }

    private void updateArtifacts(Integration config) {

        //For Inbound Endpoint
        for (InboundEndpoint inbound : config.getInbounds().values()) {
            registerInboundEndpoint(inbound);
        }

        //For Pipelines
        for (Resource resource : config.getResources().values()) {
            registerPipeline(resource);
        }

        //For Outbound Endpoints
        for (OutboundEndpoint outbound : config.getOutbounds().values()) {
            registerOutboundEndpoint(outbound);
        }


    }

    private void unDeployArtifacts(Integration configHolder) {
        //For Inbound Endpoint
        for (InboundEndpoint inbound : configHolder.getInbounds().values()) {
            unregisterInboundEndpoint(inbound);
        }

        //For Pipelines
        for (Resource resource : configHolder.getResources().values()) {
            unregisterPipeline(resource);
        }

        //For Outbound Endpoints
        for (OutboundEndpoint outbound : configHolder.getOutbounds().values()) {
            unregisterOutboundEndpoint(outbound);
        }

    }

    private void registerInboundEndpoint(InboundEndpoint inboundEndpoint) {
        inboundEndpoints.put(inboundEndpoint.getName(), inboundEndpoint);
        InboundEPDeployer deployer = InboundEPProviderRegistry.getInstance().
                getProvider(inboundEndpoint.getProtocol()).getInboundDeployer();
        if (deployer != null) {
            deployer.deploy(inboundEndpoint);
        }

        //Inform Observers
        for (ConfigRegistryObserver observer : observers) {
            observer.endpointAdded(inboundEndpoint);
        }
    }

    private void unregisterInboundEndpoint(InboundEndpoint inboundEndpoint) {

        inboundEndpoints.remove(inboundEndpoint.getName());
        InboundEPDeployer deployer = InboundEPProviderRegistry.getInstance().
                getProvider(inboundEndpoint.getProtocol()).getInboundDeployer();
        if (deployer != null) {
            deployer.undeploy(inboundEndpoint);
        }

        //Inform Observers
        for (ConfigRegistryObserver observer : observers) {
            observer.endpointRemoved(inboundEndpoint);
        }
    }

    public InboundEndpoint getInboundEndpoint(String name) {
        return inboundEndpoints.get(name);
    }

    public void registerObserver(ConfigRegistryObserver observer) {
        observers.add(observer);
    }

    public void unregisterObserver(ConfigRegistryObserver observer) {
        observers.remove(observer);
    }

    public void registerPipeline(Resource resource) {
        resources.put(resource.getName(), resource);
    }

    public void unregisterPipeline(Resource resource) {
        resources.remove(resource.getName());
    }

    public Resource getResource(String name) {
        return resources.get(name);
    }

    public OutboundEndpoint getOutboundEndpoint(String key) {
        return outBoundEndpointMap.get(key);
    }

    public void registerOutboundEndpoint(OutboundEndpoint outboundEndpoint) {
        outBoundEndpointMap.put(outboundEndpoint.getName(), outboundEndpoint);
    }

    public void unregisterOutboundEndpoint(OutboundEndpoint outboundEndpoint) {
        outBoundEndpointMap.remove(outboundEndpoint.getName());
    }

    public TypeConverterRegistry getTypeConverterRegistry() {
        return BaseTypeConverterRegistry.getInstance();
    }

}
