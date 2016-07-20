package org.wso2.carbon.gateway.core.config.annotations.resource;

import org.wso2.carbon.gateway.core.config.ConfigConstants;
import org.wso2.carbon.gateway.core.config.annotations.Annotation;
import org.wso2.carbon.gateway.core.config.annotations.resource.impl.EndpointTrigger;

public class Trigger extends Annotation<EndpointTrigger> {

    private EndpointTrigger endpointTrigger;

    public Trigger() {
        super(ConfigConstants.AN_TRIGGER);
    }

    public Trigger(EndpointTrigger endpointTrigger) {
        super(ConfigConstants.AN_TRIGGER);
        this.endpointTrigger = endpointTrigger;
    }

    @Override
    public EndpointTrigger getValue() {
        return endpointTrigger;
    }

    @Override
    public void setValue(EndpointTrigger endpointTrigger) {
        this.endpointTrigger = endpointTrigger;
    }
}
