package org.wso2.carbon.gateway.core.flow.mediators.builtin.invokers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.config.ConfigRegistry;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.AbstractMediator;
import org.wso2.carbon.gateway.core.flow.Invoker;
import org.wso2.carbon.gateway.core.flow.Worker;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageUtil;
import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controls parallel flows
 */
public class ParallelMediator extends AbstractMediator implements Invoker {
    private static final Logger log = LoggerFactory.getLogger(ParallelMediator.class);

    private String parentIntegration;
    private List<String> workers = new ArrayList<>();

    public ParallelMediator(String integration) {
        parentIntegration = integration;
    }

    public ParallelMediator(String integration, List workers) {
        parentIntegration = integration;
        this.workers = workers;
    }

    public void setParameters(ParameterHolder parameterHolder) {
        String strWorkers = parameterHolder.getParameter("workers").getValue();
        workers = Arrays.asList(strWorkers.split("\\s*,\\s*"));
    }

    @Override
    public String getName() {
        return "parallel";
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        workers.stream()
                .forEach(w -> {
                    CarbonMessage cMsg = MessageUtil.cloneCarbonMessageWithData(carbonMessage);
                    Observable observable = createObserver(w, cMsg, carbonCallback);

                    if (carbonMessage.getProperty("OBSERVABLES_MAP") == null) {
                        Map<String, Observable> observableMap = new HashMap<>();
                        carbonMessage.setProperty("OBSERVABLES_MAP", observableMap);
                    }

                    Map<String, Observable> observableMap =
                            (Map<String, Observable>) carbonMessage.getProperty("OBSERVABLES_MAP");
                    observableMap.put(String.valueOf(observable.hashCode()), observable);
                });

        return true;
    }

    private Observable createObserver(String worker, CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        Worker w = ConfigRegistry.getInstance().getIntegrationConfig(parentIntegration).getWorker(worker);
        return w.submit(UUID.randomUUID(), carbonMessage, carbonCallback);
    }
}
