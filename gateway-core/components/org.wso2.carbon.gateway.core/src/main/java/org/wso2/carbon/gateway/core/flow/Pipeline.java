/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.config.ConfigRegistry;
import org.wso2.carbon.gateway.core.exception.ErrorHandler;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * A Class representing collection of Mediators
 */
public class Pipeline {
    // TODO: Do we need this anymore

    private String name;

    /* Mediator collection */
    MediatorCollection mediators;

    /* Error handling mediator collection */
    MediatorCollection errorHandlerMediators;

    private String errorPipeline;

    private static final Logger log = LoggerFactory.getLogger(Pipeline.class);

    public Pipeline(String name) {
        this.name = name;
        this.mediators = new MediatorCollection();
    }

    public Pipeline(String name, MediatorCollection mediators) {
        this.mediators = mediators;
        this.name = name;
    }

    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        try {
            // For Error handling
            if (errorPipeline != null) {
                Pipeline ePipeline = ConfigRegistry.getInstance().getPipeline(errorPipeline);
                if (ePipeline == null) {
                    log.error("Cannot load pipeline defined as " + errorPipeline);
                    return false;
                }

                errorHandlerMediators = ePipeline.getMediators();
                if (errorHandlerMediators != null && errorHandlerMediators.getMediators().size() > 0) {
                    carbonMessage.getFaultHandlerStack().push
                            (new ErrorHandler(Constants.ERROR_HANDLER, errorHandlerMediators));
                }

            }

            return mediators.getFirstMediator().receive(carbonMessage, carbonCallback);
        } catch (Exception e) {
            log.error("Error while mediating", e);
            return false;
        }
    }

    public void addMediator(Mediator mediator) {
        mediators.addMediator(mediator);
    }

    public String getName() {
        return name;
    }

    public void setErrorPipeline(String errorPipeline) {
        this.errorPipeline = errorPipeline;
    }

    public MediatorCollection getMediators() {
        return mediators;
    }
}
