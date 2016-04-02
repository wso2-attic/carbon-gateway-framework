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

import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;


/**
 * Interface for Mediators
 */
public interface Mediator {

    /**
     * Get the name of the Mediator
     *
     * @return mediator name
     */
    String getName();

    /**
     * Set the pointer to the next sibling in the pipeline
     *
     * @param nextMediator Next sibling mediator in the pipeline
     */
    void setNext(Mediator nextMediator);

    /**
     * Check whether a sibling is present after this in the pipeline
     *
     * @return whether a sibling is present after this
     */
    boolean hasNext();

    /**
     * Invoke the next sibling in the pipeline
     *
     * @param carbonMessage  Carbon Message
     * @param carbonCallback Incoming Callback
     * @return whether mediation is proceeded
     * @throws Exception
     */
    boolean next(CarbonMessage carbonMessage, CarbonCallback carbonCallback)
            throws Exception;

    /**
     * Receive the message
     *
     * @param carbonMessage  Carbon Message
     * @param carbonCallback Incoming Callback
     * @return Whether mediation is proceeded
     * @throws Exception
     */
    boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws
                                                                                Exception;

    /**
     * Set Mediator Configurations
     *
     * @param parameters Parameters
     */
    void setParameters(ParameterHolder parameters);
}
