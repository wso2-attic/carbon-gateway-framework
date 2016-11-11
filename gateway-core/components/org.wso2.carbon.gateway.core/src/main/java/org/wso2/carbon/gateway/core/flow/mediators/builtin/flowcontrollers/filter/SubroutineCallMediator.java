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
package org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.config.IntegrationConfigRegistry;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.AbstractFlowController;
import org.wso2.carbon.gateway.core.flow.FlowControllerSubroutineCallback;
import org.wso2.carbon.gateway.core.flow.Subroutine;
import org.wso2.carbon.gateway.core.util.VariableUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Mediator that handles sub-routine calls in the Integration Configuration
 */
public class SubroutineCallMediator extends AbstractFlowController {

    private static final Logger log = LoggerFactory.getLogger(SubroutineCallMediator.class);

    /**
     * Name of the subroutine
     */
    private String subroutineId;

    /**
     * Name of the Integration
     */
    private String integrationId;

    /**
     * List of input parameter identifiers
     */
    //TODO: This list should be able to contain any type of object.
    //TODO: For now only identifiers are supported inside subroutine calls
    private List<String> inputParameters = new ArrayList<>();

    /**
     * List of return value identifiers
     */
    private List<String> returnValueIdentifiers = new ArrayList<>();

    /**
     * Constructor
     */
    public SubroutineCallMediator() {
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        /* SubroutineCall can be either inside an Integration object or inside another Global level Subroutine
           First we will check if its inside an Integration, if so first look for the Subroutine implementation in its
           Integrations' local Map
          */
        Subroutine referredSubroutine = null;
        if (this.integrationId != null) {
            referredSubroutine = IntegrationConfigRegistry.getInstance().getIntegrationConfig(this.integrationId)
                    .getSubroutine(this.subroutineId);
        }
        //TODO:if referredSubroutine is not found we should look in the global level, global level Map should implement

        // if incorrect number of arguments are given or subroutine is not present, skip the subroutine call
        if (referredSubroutine == null || referredSubroutine.getInputArgs().size() != inputParameters.size()) {
            log.error("Invalid subroutine call to " + subroutineId);
            return next(carbonMessage, carbonCallback);
        }

        // Retrieve input parameter objects
        List<Object> inputParameterObjects = new ArrayList<>();
        inputParameters.forEach(
                inputParameter -> inputParameterObjects.add(getObjectFromContext(carbonMessage, inputParameter)));

        super.receive(carbonMessage, carbonCallback);
        VariableUtil.removeParentMap(carbonMessage);

        // Put retrieved objects to the new map
        Iterator<String> subroutineArguments = referredSubroutine.getInputArgs().keySet().iterator();
        inputParameterObjects.forEach(inputParameterObject -> VariableUtil
                .addVariable(carbonMessage, subroutineArguments.next(), inputParameterObject));

        // Forward the carbon message to Subroutines' MediatorCollection with new FlowControllerSubroutineCallback
        CarbonCallback callback = new FlowControllerSubroutineCallback(carbonCallback, this,
                VariableUtil.getVariableStack(carbonMessage), referredSubroutine);
        referredSubroutine.getSubroutineMediators().getFirstMediator().receive(carbonMessage, callback);

        return true;
    }

    @Override
    public void setParameters(ParameterHolder parameterHolder) {
        this.subroutineId = parameterHolder.getParameter("subroutineId").getValue();
    }

    @Override
    public String getName() {
        return "subroutinecall";
    }

    /**
     * Set the Subroutine name
     *
     * @param subroutineId
     */
    public void setSubroutineId(String subroutineId) {
        this.subroutineId = subroutineId;
    }

    /**
     * Get Subroutine name
     *
     * @return
     */
    public String getSubroutineId() {
        return this.subroutineId;
    }

    /**
     * Set the Integration name
     *
     * @param integrationId
     */
    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }

    /**
     * Get Integration Name
     *
     * @return
     */
    public String getIntegrationId() {
        return this.integrationId;
    }

    /**
     * Set the list of return values assigning variable names
     *
     * @param returnValueIdentifiers
     */
    public void setReturnValueIdentifiers(List<String> returnValueIdentifiers) {
        this.returnValueIdentifiers = returnValueIdentifiers;
    }

    /**
     * Get the list of return values assigning variable names
     *
     * @return
     */
    public List<String> getReturnValueIdentifiers() {
        return this.returnValueIdentifiers;
    }

    /**
     * Set input arguments names which are used when calling the subroutine
     *
     * @param inputParameters
     */
    public void setInputParameters(List<String> inputParameters) {
        this.inputParameters = inputParameters;
    }

}
