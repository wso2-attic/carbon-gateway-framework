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
import org.wso2.carbon.gateway.core.flow.Mediator;
import org.wso2.carbon.gateway.core.flow.RxContext;
import org.wso2.carbon.gateway.core.util.VariableUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import rx.Observer;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of observer for each worker
 */
public class JoinObserver implements Observer<RxContext> {

    private static final Logger log = LoggerFactory.getLogger(JoinObserver.class);

    private String refName; //Referred fork name
    private Mediator mediator; //related join mediator
    private CarbonMessage incomingMsg;
    private CarbonCallback parentClbk;
    private Map<String, CarbonMessage> resultMsgMap;

    /**
     *
     * @param referenceName Fork name referred by the Join mediator
     * @param parentCallback parent callback received
     * @param joinMediator reference related join mediator
     * @param incomingCMsg incoming carbonMessage received by Join mediator
     */
    public JoinObserver(String referenceName, CarbonCallback parentCallback, Mediator joinMediator,
                                                                    CarbonMessage incomingCMsg) {
        this.refName = referenceName;
        this.parentClbk = parentCallback;
        this.mediator = joinMediator;
        this.incomingMsg = incomingCMsg;
        this.resultMsgMap = new HashMap<>();
    }

    @Override
    public void onCompleted() {
        if (log.isDebugEnabled()) {
            log.debug("Joining Fork : " + refName + " completed");
        }
        try {
            //TODO need to create new variable type to hold results or add array support for variables
            //TODO Till that support added to the engine add the results map referred by fork name
            VariableUtil.addVariable(incomingMsg, refName, resultMsgMap);
            mediator.next(incomingMsg, parentClbk);
        } catch (Exception e) {
            log.error("Error occurred while executing next mediator", e);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error occurred while observing worker flows", throwable);
    }

    @Override
    public void onNext(RxContext rxContext) {
        if (log.isDebugEnabled()) {
            log.debug("Joining Fork : " + refName + " received worker flow completed notification for worker flow : " +
                    (rxContext.getName() != null ? rxContext.getName() : rxContext.getId()));
        }
        resultMsgMap.put(rxContext.getName() != null ? rxContext.getName() : rxContext.getId(),
                rxContext.getCarbonMessage());
    }
}
