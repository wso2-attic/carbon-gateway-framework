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
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.AbstractFlowController;
import org.wso2.carbon.gateway.core.flow.MediatorCollection;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Mediator that handles sub-routine calls in the Integration Configuration
 */
public class SubroutineCallMediator extends AbstractFlowController {

    private static final Logger log = LoggerFactory.getLogger(SubroutineCallMediator.class);

    /**
     * Mediation collection to hold mediators inside subroutine
     */
    private MediatorCollection subroutineMediators;

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
    private List<String> inputParameters = new ArrayList<>();

    /**
     * List of return value identifiers
     */
    private List<String> returnValueIdentifiers = new ArrayList<>();

    /**
     * Constructor
     */
    public SubroutineCallMediator() {}

    @Override
    public boolean receive (CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        return next(carbonMessage, carbonCallback);
    }

    @Override
    public void setParameters(ParameterHolder parameterHolder) {
        this.subroutineId = parameterHolder.getParameter("subroutineId").getValue();
    }

    @Override
    public String getName() {
        return "subroutinecall";
    }
}
