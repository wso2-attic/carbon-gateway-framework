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
package org.wso2.ballerina.core.flow;

import org.wso2.ballerina.core.Constants;
import org.wso2.ballerina.core.config.annotations.Annotation;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
    /* Annotations of the Subroutine */
    private Map<String, Annotation> annotations = new HashMap<>();
    /* Return types of the subroutine */
    private List<Constants.TYPES> returnTypes;
    /* Return identifiers of the subroutine */
    private List<String> returnVariables;
    /* Input types and identifiers of the subroutine
    * LinkedHashMap is used to preserve the original arguments order */
    private LinkedHashMap<String, Constants.TYPES> inputArgs;
    /* Thrown Exception types in the subroutine*/
    private List<String> exceptionsList;

    /**
     * Constructor
     *
     * @param subroutineId Name of the subroutine
     */
    public Subroutine(String subroutineId) {
        this.subroutineId = subroutineId;
        this.subroutineMediators = new MediatorCollection();
    }

    /**
     * Add mediators to the subroutine mediator list
     *
     * @param mediator
     */
    public void addSubroutineMediator(Mediator mediator) {
        subroutineMediators.addMediator(mediator);
    }

    /**
     * @return Mediator collection of the subroutine
     */
    public MediatorCollection getSubroutineMediators() {
        return this.subroutineMediators;
    }

    /**
     * @return Name of the subroutine
     */
    public String getSubroutineId() {
        return this.subroutineId;
    }

    /**
     * @param subroutineId Name od the subroutine
     */
    public void setSubroutineId(String subroutineId) {
        this.subroutineId = subroutineId;
    }

    /**
     * Get Input arguments map
     *
     * @return
     */
    public LinkedHashMap<String, Constants.TYPES> getInputArgs() {
        return this.inputArgs;
    }

    /**
     * Setting inputArgs Map
     *
     * @param inputArgs
     */
    public void setInputArgs(LinkedHashMap<String, Constants.TYPES> inputArgs) {
        this.inputArgs = inputArgs;
    }

    /**
     * Get return types list
     *
     * @return
     */
    public List<Constants.TYPES> getReturnTypes() {
        return this.returnTypes;
    }

    /**
     * Setting returnTypes List
     *
     * @param returnTypes
     */
    public void setReturnTypes(List<Constants.TYPES> returnTypes) {
        this.returnTypes = returnTypes;
    }

    /**
     * Get exceptions list
     *
     * @return
     */
    public List<String> getExceptionsList() {
        return this.exceptionsList;
    }

    /**
     * Setting exception list that is thrown by this subroutine
     *
     * @param exceptionsList
     */
    public void setExceptionsList(List<String> exceptionsList) {
        this.exceptionsList = exceptionsList;
    }

    /**
     * Get identifiers of returning variables
     *
     * @return
     */
    public List<String> getReturnVariables() {
        return this.returnVariables;
    }

    /**
     * Setting return variable names
     *
     * @param returnVariables List of return variable names
     */
    public void setReturnVariables(List<String> returnVariables) {
        this.returnVariables = returnVariables;
    }

    /**
     * Add an annotation to annotation map
     *
     * @param annotationName Name of the Annotation
     * @param annotation     Annotation Object
     */
    public void addAnnotation(String annotationName, Annotation annotation) {
        annotations.put(annotationName, annotation);
    }

    /**
     * Get a specific annotation
     *
     * @param name Name of the annotation
     * @return Annotation object
     */
    public Annotation getAnnotation(String name) {
        return annotations.get(name);
    }

    /**
     * Get the Complete Annotation Map
     *
     * @return Annotation Map
     */
    public Map<String, Annotation> getAnnotations() {
        return annotations;
    }
}
