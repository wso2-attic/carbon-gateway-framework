package org.wso2.carbon.gateway.core.flow.triggers;


/**
 * Interface for Trigger Providers
 *
 * EndpointTriggerProvider is responsible for providing an instance of a EndpointTrigger
 *
 */
public interface EndpointTriggerProvider {

    String getName();

    EndpointTrigger getTrigger();

}
