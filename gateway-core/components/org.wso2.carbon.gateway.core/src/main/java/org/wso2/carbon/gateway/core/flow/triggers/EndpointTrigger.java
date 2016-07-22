package org.wso2.carbon.gateway.core.flow.triggers;

/**
 * EndpointTrigger implementation base class.
 */
public abstract class EndpointTrigger {

    private String name;

    public EndpointTrigger(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
