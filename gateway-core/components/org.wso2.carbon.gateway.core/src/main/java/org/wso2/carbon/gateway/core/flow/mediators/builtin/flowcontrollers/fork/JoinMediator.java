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

package org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.fork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.AbstractMediator;
import org.wso2.carbon.gateway.core.flow.RxContext;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import rx.Observable;
import rx.subjects.AsyncSubject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Implementation of Join mediator
 */
public class JoinMediator extends AbstractMediator {

    private static final Logger log = LoggerFactory.getLogger(JoinMediator.class);
    private static final String MEDIATOR_NAME = "join";

    private String forkRef;
    private String condition;

    @Override
    public String getName() {
        return MEDIATOR_NAME;
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("[Start Mediator] : Join | joining fork : " + forkRef);
        }

        if (carbonMessage.getProperty(Constants.OBSERVABLES_MAP) != null) {
            Map<String, Observable> forkObservableMap =
                    ((Map<String, Map<String, Observable>>) carbonMessage.getProperty(Constants.OBSERVABLES_MAP)).
                            get(forkRef);

            if (forkObservableMap == null) {
                throw new Exception("Observables related to Fork : " + forkRef + " not found");
            }

            List<Observable<RxContext>> observableList = new ArrayList<>();

            forkObservableMap.forEach((name, observable) -> {
                observableList.add(observable);
            });

            if (log.isDebugEnabled()) {
                log.debug("Executing Join for fork ID = " + forkRef + " with condition = " + condition);
            }

            //now clear to perform join
            if (condition.equalsIgnoreCase(Constants.CONDITION_AND)) {
                /*Condition AND : This condition wait till all the created worker integration flows to finish and
                join the main integration flow.*/
                AsyncSubject<RxContext> asyncSubject = AsyncSubject.create();
                asyncSubject.
                        merge(observableList).
                        subscribe(new JoinObserver(forkRef, carbonCallback, this, carbonMessage));

            } else if (condition.equalsIgnoreCase(Constants.CONDITION_OR)) {
                /*Condition OR : wait till at least one created worker integration flow to finish and join the main
                integration flow. Incomplete/unfinished worker integration flows are discarded.*/
                AsyncSubject<RxContext> asyncSubject = AsyncSubject.create();
                asyncSubject.
                        merge(observableList).
                        first().
                        subscribe(new JoinObserver(forkRef, carbonCallback, this, carbonMessage));

            } else if (condition.toUpperCase(Locale.getDefault()).startsWith(Constants.CONDITION_ANY_N)) {
                /*Condition ANY n : This condition waits till any n number of worker flows to finish, and join to
                the main flow.*/
                int anyN = Integer.parseInt(condition.substring(Constants.CONDITION_ANY_N.length()).replace(" ", ""));
                AsyncSubject<RxContext> asyncSubject = AsyncSubject.create();
                asyncSubject.
                        merge(observableList).
                        take(anyN).
                        subscribe(new JoinObserver(forkRef, carbonCallback, this, carbonMessage));
            } else {
                throw new Exception("Unrecognized Join condition : " + condition);
            }

        } else {
            //No observables found, this may happen due to custom mediator alter the carbon message or erroneous
            // integration flow design (join without a related fork) or multiple join calls referring same fork
            throw new Exception("No observables found");
        }

        return false;
    }

    @Override
    public void setParameters(ParameterHolder parameterHolder) {
        if (parameterHolder.getParameter(Constants.FORK_KEY) != null) {
            forkRef = parameterHolder.getParameter(Constants.FORK_KEY).getValue();
        } else {
            log.error(Constants.FORK_KEY + " is not set in the configuration.");
        }

        if (parameterHolder.getParameter(Constants.CONDITION) != null) {
            condition = parameterHolder.getParameter(Constants.CONDITION).getValue();
        } else {
            log.error(Constants.CONDITION + " is not set in the configuration.");
        }
    }
}
