package org.wso2.carbon.gateway.core.flow;

import org.wso2.carbon.gateway.core.config.ConfigConstants;
import org.wso2.carbon.gateway.core.config.annotations.Annotation;
import org.wso2.carbon.gateway.core.config.annotations.common.Description;
import org.wso2.carbon.gateway.core.config.annotations.resource.Trigger;
import org.wso2.carbon.gateway.core.config.annotations.resource.impl.EndpointTrigger;
import org.wso2.carbon.gateway.core.config.annotations.resource.impl.HTTPEndpointTrigger;
import org.wso2.carbon.gateway.core.flow.templates.uri.URITemplate;
import org.wso2.carbon.gateway.core.inbound.InboundEndpoint;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Object model representing a single resource.
 */
public class Resource {

    /**
     * Metadata holders
     */
    private String name;
    private Map<String, Annotation> annotations = new HashMap<>();


    public Resource(String name, InboundEndpoint source, String condition, URITemplate uriTemplate) {
        this.name = name;

        annotations.put(ConfigConstants.AN_DESCRIPTION, new Description());
        annotations.put(ConfigConstants.AN_TRIGGER, new Trigger(getTriggerInstance(source, condition, uriTemplate)));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        return true;
    }

    private EndpointTrigger getTriggerInstance(InboundEndpoint source, String condition, URITemplate uriTemplate) {
        String protocol = source.getProtocol();

        switch (protocol) {
            case "HTTP":
                return new HTTPEndpointTrigger(source, condition, uriTemplate);
            default:
                return new HTTPEndpointTrigger(source, condition, uriTemplate);
        }
    }
}
