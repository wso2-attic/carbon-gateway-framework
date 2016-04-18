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

import org.wso2.carbon.gateway.core.flow.Group;
import org.wso2.carbon.gateway.core.flow.Pipeline;
import org.wso2.carbon.gateway.core.inbound.InboundEndpoint;
import org.wso2.carbon.gateway.core.outbound.OutboundEndpoint;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Object Model which holds configurations related to a one GW process
 */
public class GWConfigHolder {

    private String name;

    private InboundEndpoint inboundEndpoint;

    private Map<String, Pipeline> pipelines = new HashMap<>();

    private Map<String, Group> groups = new HashMap<>();

    private Map<String, OutboundEndpoint> outboundEndpoints = new HashMap<>();

    private Map<String, Object> globalVariables = new HashMap<>();

    public GWConfigHolder(String name) {
        this.name = name;
        globalVariables.put("iflowName", name); //TODO: demo code only, remove
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGlobalVariables(Map<String, Object> globalVariables) {
        this.globalVariables = globalVariables;
    }

    public InboundEndpoint getInboundEndpoint() {
        return inboundEndpoint;
    }

    public void setInboundEndpoint(InboundEndpoint inboundEndpoint) {
        inboundEndpoint.setGWConfigName(name);
        this.inboundEndpoint = inboundEndpoint;
    }

    public Pipeline getPipeline(String name) {
        return pipelines.get(name);
    }

    public void addPipeline(Pipeline pipeline) {
        pipelines.put(pipeline.getName(), pipeline);
    }

    public Map<String, Pipeline> getPipelines() {
        return pipelines;
    }

    public Map<String, OutboundEndpoint> getOutboundEndpoints() {
        return outboundEndpoints;
    }

    public OutboundEndpoint getOutboundEndpoint(String name) {
        return outboundEndpoints.get(name);
    }

    public Map<String, Object> getGlobalVariables() {
        return globalVariables;
    }

    public void addOutboundEndpoint(OutboundEndpoint outboundEndpoint) {
        outboundEndpoints.put(outboundEndpoint.getName(), outboundEndpoint);
    }

    public void addGroup(Group group) {
        groups.put(group.getPath(), group);
    }

    public Collection<Group> getGroups() {
        return groups.values();
    }

    public Group getGroup(String path) {
        return groups.get(path);
    }

    public boolean hasGroups() {
        return !groups.isEmpty();
    }

}
