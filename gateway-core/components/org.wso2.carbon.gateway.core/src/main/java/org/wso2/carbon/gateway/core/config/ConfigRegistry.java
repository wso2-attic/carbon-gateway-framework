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


import org.wso2.carbon.gateway.core.flow.Pipeline;
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
public class ConfigRegistry {


    private static ConfigRegistry configRegistry = new ConfigRegistry();

    private Map<String, InboundEndpoint> inboundEndpoints = new HashMap<>();

    private Map<String, Pipeline> pipelines = new HashMap<>();

    private Map<String, OutboundEndpoint> outBoundEndpointMap = new HashMap<>();

    private List<ConfigRegistryObserver> observers = new ArrayList<>();

    private Map<String, GWConfigHolder> configurations = new HashMap<>();

    public static ConfigRegistry getInstance() {
        return configRegistry;
    }

    private ConfigRegistry() {
    }

    /**
     * Add a Gateway Artifact Configuration to the Registry
     *
     * @param configHolder a Gateway Artifact
     */
    public void addGWConfig(GWConfigHolder configHolder) {
        configurations.put(configHolder.getName(), configHolder);
        updateArtifacts(configHolder);
    }

    /**
     * Remove a Gateway Artifact configuration
     *
     * @param configHolder a Gateway Artifact
     */
    public void removeGWConfig(GWConfigHolder configHolder) {
        configurations.remove(configHolder.getName());
        unDeployArtifacts(configHolder);
    }

    public GWConfigHolder getGWConfig(String name) {
        return configurations.get(name);
    }

    private void updateArtifacts(GWConfigHolder config) {

        //For Inbound Endpoint
        InboundEndpoint inboundEndpoint = config.getInboundEndpoint();
        if (inboundEndpoint != null) {
            registerInboundEndpoint(inboundEndpoint);
        }

        //For Pipelines
        for (Pipeline pipeline : config.getPipelines().values()) {
            registerPipeline(pipeline);
        }

        //For Outbound Endpoints
        for (OutboundEndpoint outboundEndpoint : config.getOutboundEndpoints().values()) {
            registerOutboundEndpoint(outboundEndpoint);
        }


    }

    private void unDeployArtifacts(GWConfigHolder configHolder) {
        //For Inbound Endpoint
        InboundEndpoint inboundEndpoint = configHolder.getInboundEndpoint();
        if (inboundEndpoint != null) {
            unregisterInboundEndpoint(inboundEndpoint);
        }

        //For Pipelines
        for (Pipeline pipeline : configHolder.getPipelines().values()) {
            unregisterPipeline(pipeline);
        }

        //For Outbound Endpoints
        for (OutboundEndpoint outboundEndpoint : configHolder.getOutboundEndpoints().values()) {
            unregisterOutboundEndpoint(outboundEndpoint);
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

    public void registerPipeline(Pipeline pipeline) {
        pipelines.put(pipeline.getName(), pipeline);
    }

    public void unregisterPipeline(Pipeline pipeline) {
        pipelines.remove(pipeline.getName());
    }

    public Pipeline getPipeline(String name) {
        return pipelines.get(name);
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

}
