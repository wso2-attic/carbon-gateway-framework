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
package org.wso2.ballerina.core.config;

import org.wso2.ballerina.core.Constants;
import org.wso2.ballerina.core.config.annotations.Annotation;
import org.wso2.ballerina.core.config.annotations.IAnnotation;
import org.wso2.ballerina.core.config.annotations.common.Description;
import org.wso2.ballerina.core.config.annotations.integration.Path;
import org.wso2.ballerina.core.config.annotations.integration.Security;
import org.wso2.ballerina.core.config.annotations.integration.Source;
import org.wso2.ballerina.core.flow.Resource;
import org.wso2.ballerina.core.flow.Subroutine;
import org.wso2.ballerina.core.inbound.InboundEndpoint;
import org.wso2.ballerina.core.outbound.OutboundEndpoint;
import org.wso2.ballerina.core.util.VariableUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Object model for a single CGF integration i.e. represents a single integration process running on CGF.
 */
public class Integration {

    /**
     * Metadata holders
     */
    private String name;
    private Map<String, Annotation> annotations = new HashMap<>();

    /**
     * Runtime objects
     */
    private Map<String, Object> constants = new HashMap<>();
    private Map<String, InboundEndpoint> inbounds = new HashMap<>();
    private Map<String, OutboundEndpoint> outbounds = new HashMap<>();
    private Map<String, Resource> resources = new HashMap<>();
    private Map<String, Subroutine> localSubroutines = new HashMap<>();

    public Integration(String name) {
        this.name = name;

        annotations.put(ConfigConstants.AN_BASE_PATH, new Path());
        annotations.put(ConfigConstants.AN_SECURITY, new Security());
        annotations.put(ConfigConstants.AN_DESCRIPTION, new Description());
        annotations.put(ConfigConstants.AN_SOURCE, new Source());
    }

    public String getName() {
        return name;
    }

    public Annotation getAnnotation(String name) {
        return annotations.get(name);
    }

    public Map<String, Annotation> getAnnotations() {
        return annotations;
    }

    public void addAnnotation(String name, IAnnotation value) throws AnnotationNotSupportedException {
        if (annotations.get(name) != null) {
            annotations.get(name).setValue(value);
        } else {
            throw new AnnotationNotSupportedException("Annotation " + name + " is not supported by Integration");
        }
    }

    public Object getConstant(String key) {
        return constants.get(key);
    }

    public Map<String, Object> getConstants() {
        return constants;
    }

    public void addConstant(Constants.TYPES type, String key, String value) {
        Object variable = VariableUtil.createVariable(type, value);
        constants.put(key, variable);
    }

    public InboundEndpoint getInbound(String name) {
        return inbounds.get(name);
    }

    public Map<String, InboundEndpoint> getInbounds() {
        return inbounds;
    }

    public void addInbound(InboundEndpoint inbound) {
        inbound.setConfigName(name);
        inbounds.put(inbound.getName(), inbound);
    }

    public OutboundEndpoint getOutbound(String name) {
        return outbounds.get(name);
    }

    public Map<String, OutboundEndpoint> getOutbounds() {
        return outbounds;
    }

    public void addOutbound(OutboundEndpoint outbound) {
        outbounds.put(outbound.getName(), outbound);
    }

    public Resource getResource(String name) {
        return resources.get(name);
    }

    public Map<String, Resource> getResources() {
        return resources;
    }

    public void addResource(Resource resource) {
        resources.put(resource.getName(), resource);
    }

    public void addAnnotation(String annotationName, Annotation annotation) {
        annotations.put(annotationName, annotation);
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Add a Subroutine to the local subroutine map of this Integration
     * @param subroutine
     */
    public void addSubroutine(Subroutine subroutine) {
        this.localSubroutines.put(subroutine.getSubroutineId(), subroutine);
    }

    /**
     * Search and return a Subroutine inside localSubroutineMap
     * @param subroutineId Name of the Subroutine
     * @return Subroutine if exists, null otherwise
     */
    public Subroutine getSubroutine(String subroutineId) {
        return this.localSubroutines.get(subroutineId);
    }
}
