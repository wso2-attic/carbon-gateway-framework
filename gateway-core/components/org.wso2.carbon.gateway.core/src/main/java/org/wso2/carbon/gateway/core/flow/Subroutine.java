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

import org.wso2.carbon.gateway.core.Constants;

import java.util.List;
import java.util.Map;

/**
 * Class that will hold data regarding a subroutine
 */
public class Subroutine {
    /* Mediator list in the subroutine */
    private MediatorCollection subroutineMediators;
    /* Name of the Subroutine */
    private String subroutineId;
    /* Return types of the subroutine */
    private List<Constants.TYPES> returnTypes;
    /* Return identifiers of the subroutine */
    private List<String> returnVariables;
    /* Input types and identifiers of the subroutine */
    private Map<String, Constants.TYPES> inputArgs;
    /* Thrown Exception types in the subroutine*/
    private List<String> exceptionsList;

    /**
     * Constructor
     * @param subroutineId Name of the subroutine
     */
    public Subroutine (String subroutineId) {
        this.subroutineId = subroutineId;
        this.subroutineMediators = new MediatorCollection();
    }

    /**
     * Add mediators to the subroutine mediator list
     * @param mediator
     */
    public void addSubroutineMediator(Mediator mediator) {
        subroutineMediators.addMediator(mediator);
    }

    /**
     *
     * @return Mediator collection of the subroutine
     */
    public MediatorCollection getSubroutineMediators() {
        return this.subroutineMediators;
    }

    /**
     * @param subroutineId Name od the subroutine
     */
    public void setSubroutineId(String subroutineId) {
        this.subroutineId = subroutineId;
    }

    /**
     *
     * @return Name of the subroutine
     */
    public String getSubroutineId() {
        return this.subroutineId;
    }

    /**
     * Setting inputArgs Map
     * @param inputArgs
     */
    public void setInputArgs(Map<String, Constants.TYPES> inputArgs) {
        this.inputArgs = inputArgs;
    }

    /**
     * Get Input arguments map
     * @return
     */
    public Map<String, Constants.TYPES> getInputArgs() {
        return this.inputArgs;
    }


    /**
     * Setting returnTypes List
     * @param returnTypes
     */
    public void setReturnTypes(List<Constants.TYPES> returnTypes) {
        this.returnTypes = returnTypes;
    }

    /**
     * Get return types list
     * @return
     */
    public List<Constants.TYPES> getReturnTypes() {
        return this.returnTypes;
    }

    /**
     * Setting exception list that is thrown by this subroutine
     * @param exceptionsList
     */
    public void setExceptionsList(List<String> exceptionsList) {
        this.exceptionsList = exceptionsList;
    }

    /**
     * Get exceptions list
     * @return
     */
    public List<String> getExceptionsList() {
        return this.exceptionsList;
    }

    /**
     * Setting return variable names
     * @param returnVariables List of return variable names
     */
    public void setReturnVariables(List<String> returnVariables) {
        this.returnVariables = returnVariables;
    }

    /**
     * Get identifiers of returning variables
     * @return
     */
    public List<String> getReturnVariables() {
        return this.returnVariables;
    }
}
