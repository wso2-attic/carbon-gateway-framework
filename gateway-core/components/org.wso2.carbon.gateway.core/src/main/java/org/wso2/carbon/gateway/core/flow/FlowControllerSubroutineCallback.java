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
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.SubroutineCallMediator;
import org.wso2.carbon.gateway.core.util.VariableUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Callback related to SubroutineCall Mediators
 */
public class FlowControllerSubroutineCallback implements FlowControllerCallback {

    private static final Logger log = LoggerFactory.getLogger(FlowControllerSubroutineCallback.class);
    /* Incoming callback */
    private CarbonCallback parentCallback;

    /* SubroutineCall Mediator */
    private SubroutineCallMediator mediator;

    /* Parents Variable Stack*/
    private Stack<Map<String, Object>> variableStack;

    /* Referred Subroutine by the SubroutineMediatorCall */
    private Subroutine subroutine;

    public FlowControllerSubroutineCallback(CarbonCallback parentCallback, SubroutineCallMediator mediator,
            Stack variableStack, Subroutine subroutine) {
        this.parentCallback = parentCallback;
        this.mediator = mediator;
        this.variableStack = variableStack;
        this.subroutine = subroutine;
    }

    @Override
    public void done(CarbonMessage carbonMessage) {
        if (canProcess(carbonMessage)) {
            // Retrieve returning objects from the Subroutines' scope
            List<Object> retuningObjects = new ArrayList<>();
            if (subroutine.getReturnVariables() != null) {
                subroutine.getReturnVariables().forEach(
                        identifier -> retuningObjects.add(VariableUtil.getVariable(carbonMessage, identifier)));
            }
            // Remove Subroutines' scope
            VariableUtil.popVariableStack(carbonMessage, variableStack);
            // Validate if correct number of returning values are specified at SubroutineMediatorCall
            if (mediator.getReturnValueIdentifiers().size() > retuningObjects.size()) {
                log.error("Invalid return value assignment in subroutine call " + mediator.getSubroutineId());
            }
            // Set returning objects to parents' scope
            for (int i = 0; i < Math.min(mediator.getReturnValueIdentifiers().size(), retuningObjects.size()); i++) {
                mediator.setObjectToContext(carbonMessage, mediator.getReturnValueIdentifiers().get(i),
                        retuningObjects.get(i));
            }
            if (mediator.hasNext()) {
                try {
                    mediator.next(carbonMessage, parentCallback);
                } catch (Exception e) {
                    log.error("Error while mediating from Message Callback", e);
                }
            } else if (parentCallback instanceof FlowControllerCallback) {
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

    /**
     * Get Mediator which created this Callback
     *
     * @return
     */
    public Mediator getMediator() {
        return mediator;
    }

    /**
     * Get parent of this Callback
     *
     * @return
     */
    public CarbonCallback getParentCallback() {
        return parentCallback;
    }

    @Override
    public boolean canProcess(CarbonMessage carbonMessage) {
        return !carbonMessage.isFaulty();
    }

}
