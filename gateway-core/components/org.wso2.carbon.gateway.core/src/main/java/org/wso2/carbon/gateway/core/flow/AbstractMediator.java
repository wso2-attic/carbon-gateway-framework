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
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.contentaware.ConversionManager;
import org.wso2.carbon.gateway.core.util.VariableUtil;
import org.wso2.carbon.gateway.core.worker.WorkerModelDispatcher;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Map;

/**
 * Base class for all the mediators. All the mediators must be extended from this base class
 */
public abstract class AbstractMediator implements Mediator {

    private static final Logger log = LoggerFactory.getLogger(AbstractMediator.class);

    /* Pointer for the next sibling in the pipeline*/ Mediator nextMediator = null;

    /* If this mediator call returns to a Variable, its identifier is stored here */
    protected String returnedOutput;
    /**
     * Check whether a sibling is present after this in the pipeline
     *
     * @return whether a sibling is present after this
     */
    public boolean hasNext() {
        return nextMediator != null;
    }

    /**
     * Invoke the next sibling in the pipeline
     *
     * @param carbonMessage  Carbon message
     * @param carbonCallback Incoming Callback
     * @return whether mediation is proceeded
     * @throws Exception
     */
    public boolean next(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        if (!hasNext() && carbonCallback instanceof FlowControllerCallback) {
            carbonCallback.done(carbonMessage);
        }
        return hasNext() && nextMediator.receive(carbonMessage, carbonCallback);
    }

    /**
     * Set the pointer to the next sibling in the pipeline
     *
     * @param nextMediator Next sibling mediator in the pipeline
     */
    public void setNext(Mediator nextMediator) {
        this.nextMediator = nextMediator;
    }

    /**
     * Set Mediator Configurations
     *
     * @param parameterHolder Parameters Holder
     */
    public void setParameters(ParameterHolder parameterHolder) {
        //Do nothing
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        Object obj = carbonMessage.getProperty(org.wso2.carbon.gateway.core.worker.Constants.PARENT_TYPE);
        if (obj != null) {
            String val = (String) obj;
            if (val.equals(org.wso2.carbon.gateway.core.worker.Constants.CPU_BOUND)
                    && getMediatorType() == MediatorType.IO_BOUND) {
                WorkerModelDispatcher.getInstance().
                        dispatch(carbonMessage, carbonCallback, this, MediatorType.IO_BOUND);

            } else if (val.equals(org.wso2.carbon.gateway.core.worker.Constants.IO_BOUND)
                    && getMediatorType() == MediatorType.CPU_BOUND) {
                WorkerModelDispatcher.getInstance().
                        dispatch(carbonMessage, carbonCallback, this, MediatorType.CPU_BOUND);
            }
        }
        return false;
    }

    /**
     * Convert message into a specified format
     *
     * @param cMsg       Carbon Message
     * @param targetType Type to be converted
     * @return CarbonMessage with converted message body
     * @throws Exception
     */
    public CarbonMessage convertTo(CarbonMessage cMsg, String targetType) throws Exception {

        String sourceType = cMsg.getHeader("Content-Type");
        if (sourceType == null) {
            handleException("Content-Type header could not be found in the request");
            return null; // to make findbugs happy
        }
        sourceType = sourceType.split(";")[0];  // remove charset from Content-Type header

        return ConversionManager.getInstance().convertTo(cMsg, sourceType, targetType);
    }

    public void handleException(String msg) throws Exception {
        handleException(msg, null);
    }

    public void handleException(String msg, Exception ex) throws Exception {
        if (ex != null) {
            log.error(msg, ex);
            throw new Exception(msg, ex);
        } else {
            log.error(msg);
            throw new Exception(msg);
        }
    }

    public Object getValue(CarbonMessage carbonMessage, String name) {
        if (name.startsWith("$")) {
            return VariableUtil.getVariable(carbonMessage, name.substring(1));

        } else {
            return name;
        }
    }

    /**
     * Retrieve an object from the Variable stack
     *
     * @param carbonMessage Carbon message with the stack
     * @param objectName    Name of the object
     * @return Object itself
     */
    protected Object getObjectFromContext(CarbonMessage carbonMessage, String objectName) {
        return VariableUtil.getVariable(carbonMessage, objectName);
    }

    /**
     * Put an object in the variable stack
     *
     * @param carbonMessage Carbon message with the stack
     * @param objectName    Name of the object
     * @param object        Object itself
     */
    public void setObjectToContext(CarbonMessage carbonMessage, String objectName, Object object) {
        Map map = (Map) VariableUtil.getMap(carbonMessage, objectName);
        if (map != null) {
            map.put(objectName, object);
        } else {
            log.error("Variable " + objectName + " is not declared.");
        }

    }

    @Override
    public MediatorType getMediatorType() {
        return MediatorType.IO_BOUND;
    }

    public String getReturnedOutput() {
        return this.returnedOutput;
    }

}
