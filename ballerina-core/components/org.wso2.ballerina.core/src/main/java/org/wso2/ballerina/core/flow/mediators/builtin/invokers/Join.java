package org.wso2.ballerina.core.flow.mediators.builtin.invokers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.config.ParameterHolder;
import org.wso2.ballerina.core.flow.AbstractMediator;
import org.wso2.ballerina.core.flow.Invoker;
import org.wso2.ballerina.core.flow.RxContext;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.subjects.BehaviorSubject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Receive from Worker
 */
public class Join extends AbstractMediator implements Invoker {
    private static final Logger log = LoggerFactory.getLogger(Join.class);

    private String parentIntegration;
    private List<String> workers = new ArrayList<>();
    private boolean or = false;

    public Join(String integration) {
        parentIntegration = integration;
    }

    public Join(String integration, List workers, boolean or) {
        parentIntegration = integration;
        this.workers = workers;
        this.or = or;
    }

    public void setParameters(ParameterHolder parameterHolder) {
        String strWorkers = parameterHolder.getParameter("workers").getValue();
        workers = Arrays.asList(strWorkers.split("\\s*,\\s*"));
        or = Boolean.valueOf(parameterHolder.getParameter("or").getValue());
    }

    @Override
    public String getName() {
        return "receive";
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Executing Join");
        }

        Map<String, Observable> observableMap = (Map<String, Observable>) carbonMessage.getProperty("OBSERVABLES_MAP");

        if (observableMap == null) {
            log.error("Could not find Observables map");
            return false;
        }

        if (or) {
            log.debug("Executing Join OR");

            List<Observable<RxContext>> oList = new ArrayList<>();

            observableMap.forEach((k, v) -> {
                oList.add(v);
            });

            BehaviorSubject behaviorSubject = BehaviorSubject.create();
            
            behaviorSubject.merge(oList).first().subscribe(r -> {
                try {
                    log.debug("Receive Mediator received event. " + r.getId());
                    next(r.getCarbonMessage(), r.getCarbonCallback());
                } catch (Throwable t) {
                    throw Exceptions.propagate(t);
                }
            });

        } else {
            log.debug("Executing Join non OR");

            Map.Entry<String, Observable> entry = observableMap.entrySet().iterator().next();
            if (entry.getValue() != null) {
                Observable<RxContext> o = entry.getValue();
                o.subscribe(r -> {
                    try {
                        next(r.getCarbonMessage(), r.getCarbonCallback());
                    } catch (Throwable t) {
                        throw Exceptions.propagate(t);
                    }
                });
            } else {
                log.error("Could not find observable.");
            }
        }

        log.debug("Executing Join done!");

        return false;
    }

}
