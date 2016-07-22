package org.wso2.carbon.gateway.core.flow.triggers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Store for TriggerProviders
 */
public class EndpointTriggerProviderRegistry {

    private Map<String, EndpointTriggerProvider> triggerProviders = new HashMap<>();

    private Map<String, Class> builtinTriggers = new HashMap<>();

    private static EndpointTriggerProviderRegistry
            instance = new EndpointTriggerProviderRegistry();

    private static final Logger log =
            LoggerFactory.getLogger(EndpointTriggerProviderRegistry.class);

    private final Class[] builtinTriggerClasses = {
            HTTPEndpointTrigger.class,
    };

    private EndpointTriggerProviderRegistry() {
        loadEndpointTriggers();
    }

    public static EndpointTriggerProviderRegistry getInstance() {
        return instance;
    }

    public void registerEndpointTriggerProvider(EndpointTriggerProvider triggerProvider) {
        triggerProviders.put(triggerProvider.getName(), triggerProvider);
    }

    private void loadEndpointTriggers() {
        for (Class c : builtinTriggerClasses) {
            try {
                EndpointTrigger trigger = (EndpointTrigger) c.newInstance();
                builtinTriggers.put(trigger.getName(), c);
            } catch (InstantiationException e) {
                log.error("Error while instantiating EndpointTrigger " + c.getName(), e);
            } catch (IllegalAccessException e) {
                log.error("Illegal Access error while instantiating EndpointTriggger " + c.getName(), e);
            }
        }
    }

    public void unregisterEndpointTriggerProvider(EndpointTriggerProvider triggerProvider) {
        triggerProviders.remove(triggerProvider.getName());
    }

    public EndpointTrigger getTrigger(String name) {

        Class c = builtinTriggers.get(name);

        if (c != null) {
            try {
                return (EndpointTrigger) c.newInstance();
            } catch (InstantiationException e) {
                log.error("Error while instantiation of " + name, e);
            } catch (IllegalAccessException e) {
                log.error("Illegal Access error while instantiation of " + name, e);
            }

        } else if (triggerProviders.containsKey(name)) {
            return triggerProviders.get(name).getTrigger();
        } else {
            log.error("EndpointTrigger implementation not found for " + name);
        }

        return null;
    }

}
