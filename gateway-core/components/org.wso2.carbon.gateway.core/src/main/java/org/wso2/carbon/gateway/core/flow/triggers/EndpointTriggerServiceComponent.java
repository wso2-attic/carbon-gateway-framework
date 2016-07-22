package org.wso2.carbon.gateway.core.flow.triggers;

import org.osgi.framework.BundleContext;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

/**
 * Service Component for EndpointTriggerProvider
 *
 * This will wait until all the TriggerProviders are available and register TriggerRegistry as a
 * service so that others can consume it.
 *
 */
@Component(
        name = "org.wso2.carbon.gateway.core.flow.triggers.EndpointTriggerServiceComponent",
        immediate = true,
        property = {
                "componentName=trigger-provider"
        }
)
public class EndpointTriggerServiceComponent implements RequiredCapabilityListener {

    private static final Logger logger =
            LoggerFactory.getLogger(EndpointTriggerServiceComponent.class);

    private BundleContext bundleContext;

    private boolean isAllProviderAvailable;

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

        if (isAllProviderAvailable) {
            bundleContext.registerService(EndpointTriggerProviderRegistry.class,
                    EndpointTriggerProviderRegistry.getInstance(), null);
        }
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        if (logger.isDebugEnabled()) {
            logger.debug("All Trigger Providers available");
        }

        isAllProviderAvailable = true;

        if (bundleContext != null) {
            bundleContext.registerService(EndpointTriggerProviderRegistry.class,
                    EndpointTriggerProviderRegistry.getInstance(), null);
        }
    }

    @Reference(
            name = "Trigger-Service",
            service = EndpointTriggerProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterEndpointTriggerProvider"
    )
    protected void registerEndpointTriggerProvider(EndpointTriggerProvider triggerProvider) {
        EndpointTriggerProviderRegistry.getInstance().registerEndpointTriggerProvider(triggerProvider);
    }

    protected void unregisterEndpointTriggerProvider(EndpointTriggerProvider triggerProvider) {
        EndpointTriggerProviderRegistry.getInstance().unregisterEndpointTriggerProvider(triggerProvider);
    }
}
