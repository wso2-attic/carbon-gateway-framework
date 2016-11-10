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
import org.wso2.carbon.gateway.core.flow.AbstractFlowController;
import org.wso2.carbon.gateway.core.flow.Mediator;
import org.wso2.carbon.gateway.core.flow.Worker;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageUtil;
import rx.Observable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of Fork mediator
 */
public class ForkMediator extends AbstractFlowController {

    private static final Logger log = LoggerFactory.getLogger(ForkMediator.class);
    private static final String MEDIATOR_NAME = "fork";

    private List<Worker> workerFlows = new ArrayList<>();
    private String forkName;
    private String messageKey;

    public ForkMediator(String forkName) {
        this.forkName = forkName;
    }

    @Override
    public String getName() {
        return MEDIATOR_NAME;
    }

    @Override
    public boolean receive(CarbonMessage cMsg, CarbonCallback carbonCallback) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("[Start Mediator] : Fork | Forking : " + forkName);
        }

        //Start each worker flow
        workerFlows.stream().forEach(workerBlock -> {
            if (log.isDebugEnabled()) {
                log.debug("Start Executing worker flow : " + workerBlock.getName());
            }

            //retrieve referenced carbon message and clone it with data
            CarbonMessage clonedCMsg = MessageUtil.
                    cloneCarbonMessageWithOutData((CarbonMessage) getObjectFromContext(cMsg, messageKey));

            //create callback for each worker to notify when worker flow execution completed
            UUID uuid = UUID.randomUUID();
            ObservableControllerCallback callback = new ObservableControllerCallback(workerBlock.getName(), uuid);
            Observable observable = workerBlock.submit(uuid, clonedCMsg, callback);

            /*
             * Within carbon message we keep a Map as a property to hold observables related to each Fork
             * Each entry in the OBSERVABLES_MAP contain map containing observables related to each Fork against
             * Fork name
             */
            //create observable map if not exists
            if (cMsg.getProperty(Constants.OBSERVABLES_MAP) == null) {
                Map<String, Map<String, Observable>> observableMap = new LinkedHashMap<>();
                cMsg.setProperty(Constants.OBSERVABLES_MAP, observableMap);
            }

            //adding observable to the list in the map under the name of the fork
            Map<String, Observable> forkObservableMap =
                    ((Map<String, Map<String, Observable>>) cMsg.getProperty(Constants.OBSERVABLES_MAP)).get(forkName);

            if (forkObservableMap == null) {
                //create and add observable map for this fork
                log.debug("Observable map for the Fork does not exists. Hence creating new map");
                forkObservableMap = new LinkedHashMap<>();
                ((Map<String, Map<String, Observable>>) cMsg.getProperty(Constants.OBSERVABLES_MAP)).
                        put(forkName, forkObservableMap);
            }
            forkObservableMap.put(workerBlock.getName(), observable);
        });

        //Continue main integration flow
        return next(cMsg, carbonCallback);
    }

    @Override
    public void setParameters(ParameterHolder parameterHolder) {
        if (parameterHolder.getParameter(Constants.MESSAGE_KEY) != null) {
            messageKey = parameterHolder.getParameter(Constants.MESSAGE_KEY).getValue();
        } else {
            log.error(Constants.MESSAGE_KEY + " is not set in the configuration.");
        }
    }

    /**
     * Function to retrieve Name of the Fork
     *
     * @return name of the fork block
     */
    public String getForkName() {
        return forkName;
    }

    /**
     * Function to add worker block to the fork
     *
     * @param workerBlock Worker object need to add
     */
    public void addWorkerBlock(Worker workerBlock) {
        workerFlows.add(workerBlock);
    }

    /**
     * Function to add mediator to the last worker in the Worker List
     *
     * @param mediator Mediator object to add to the last worker
     */
    public void addMediatorToLastWorker (Mediator mediator) {
        workerFlows.get(workerFlows.size() - 1).addMediator(mediator);
    }
}
