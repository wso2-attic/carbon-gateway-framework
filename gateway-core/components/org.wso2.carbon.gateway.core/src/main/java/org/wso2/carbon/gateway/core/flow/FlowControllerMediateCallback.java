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

package org.wso2.carbon.gateway.core.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.exception.DefaultExceptionHandler;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.invokers.CallMediator;
import org.wso2.carbon.gateway.core.util.VariableUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Map;
import java.util.Stack;

/**
 * Callback related to AbstractFlowController Mediators
 */
public class FlowControllerMediateCallback implements FlowControllerCallback {

    /* Incoming callback */
    CarbonCallback parentCallback;

    /* Flow Controller Mediator */
    Mediator mediator;

    Stack<Map<String, Object>> variableStack;

    private static final Logger log = LoggerFactory.getLogger(FlowControllerMediateCallback.class);

    public FlowControllerMediateCallback(CarbonCallback parentCallback, Mediator mediator, Stack variableStack) {
        this.parentCallback = parentCallback;
        this.mediator = mediator;
        this.variableStack = variableStack;
    }

    @Override
    public void done(CarbonMessage carbonMessage) {

        if (canProcess(carbonMessage)) {
            VariableUtil.popVariableStack(carbonMessage, variableStack);
            if (mediator.hasNext()) { // If Mediator has a sibling after this
                try {
                    if (mediator instanceof CallMediator) {
                        ((CallMediator) mediator)
                                .setObjectToContext(carbonMessage, ((CallMediator) mediator).getReturnedOutput(),
                                        carbonMessage);
                    }
                    mediator.next(carbonMessage, parentCallback);
                } catch (Exception e) {
                    log.error("Error while mediating from Callback", e);
                }
            } else if (parentCallback instanceof FlowControllerCallback) {
                //If no siblings handover message to the requester
                parentCallback.done(carbonMessage);
            } else {
                log.warn("Chain was completed without sending a response to client");
            }
        } else {
            if (parentCallback instanceof FlowControllerCallback) {
                parentCallback.done(carbonMessage);
            } else {
                new DefaultExceptionHandler().handleException(carbonMessage, parentCallback);
            }
        }
    }

    public Mediator getMediator() {
        return mediator;
    }

    public CarbonCallback getParentCallback() {
        return parentCallback;
    }

    @Override
    public boolean canProcess(CarbonMessage carbonMessage) {
        return !carbonMessage.isFaulty();
    }
}
