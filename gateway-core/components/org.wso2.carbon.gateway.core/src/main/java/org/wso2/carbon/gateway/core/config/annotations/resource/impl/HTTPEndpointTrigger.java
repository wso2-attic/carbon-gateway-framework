package org.wso2.carbon.gateway.core.config.annotations.resource.impl;

import org.wso2.carbon.gateway.core.flow.templates.uri.URITemplate;
import org.wso2.carbon.gateway.core.inbound.InboundEndpoint;

public class HTTPEndpointTrigger extends EndpointTrigger {

    private InboundEndpoint source;
    private String condition;
    private URITemplate path;

    public HTTPEndpointTrigger(InboundEndpoint source, String condition, URITemplate path) {
        this.source = source;
        this.condition = condition;
        this.path = path;
    }

    public InboundEndpoint getSource() {
        return source;
    }

    public void setSource(InboundEndpoint source) {
        this.source = source;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public URITemplate getPath() {
        return path;
    }

    public void setPath(URITemplate path) {
        this.path = path;
    }
}
