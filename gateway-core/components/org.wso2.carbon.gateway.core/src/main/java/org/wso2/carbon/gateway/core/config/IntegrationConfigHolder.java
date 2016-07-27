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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.flow.Group;
import org.wso2.carbon.gateway.core.flow.Worker;
import org.wso2.carbon.gateway.core.inbound.InboundEndpoint;
import org.wso2.carbon.gateway.core.outbound.OutboundEndpoint;
import org.wso2.carbon.gateway.core.util.VariableUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Object Model which holds configurations related to a one GW process
 */
public class IntegrationConfigHolder {
    private static final Logger log = LoggerFactory.getLogger(IntegrationConfigHolder.class);

    private String name;

    private InboundEndpoint inboundEndpoint;

    private Map<String, Worker> workers = new HashMap<>();

    private Map<String, Group> groups = new HashMap<>();

    private Map<String, OutboundEndpoint> outboundEndpoints = new HashMap<>();

    private Map<String, Object> globalConstants = new HashMap<>();

    public IntegrationConfigHolder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addGlobalConstant(Constants.TYPES type, String key, String value) {
        Object variable = VariableUtil.createVariable(type, value);
        globalConstants.put(key, variable);
    }

    public Object getGlobalConstant(String key) {
        return globalConstants.get(key);
    }

    public void removeGlobalConstant(String key) {
        globalConstants.remove(key);
    }

    public InboundEndpoint getInboundEndpoint() {
        return inboundEndpoint;
    }

    public void setInboundEndpoint(InboundEndpoint inboundEndpoint) {
        inboundEndpoint.setConfigName(name);
        this.inboundEndpoint = inboundEndpoint;
    }

    public Worker getWorker(String name) {
        return workers.get(name);
    }

    public void addWorker(Worker worker) {
        workers.put(worker.getName(), worker);
    }

    public Map<String, Worker> getWorkers() {
        return workers;
    }

    public Map<String, OutboundEndpoint> getOutboundEndpoints() {
        return outboundEndpoints;
    }

    public OutboundEndpoint getOutboundEndpoint(String name) {
        return outboundEndpoints.get(name);
    }

    public Map<String, Object> getGlobalConstants() {
        return globalConstants;
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
