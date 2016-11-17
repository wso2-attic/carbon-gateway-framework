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
package org.wso2.ballerina.core.inbound;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;


/**
 * Message Dispatcher Interface. Need to have a protocol specific dispatcher
 */
public interface Dispatcher {

    /**
     * Dispatch the message to an inbound endpoint
     *
     * @param carbonMessage Carbon Message
     * @param callback      Callback
     * @return whether dispatching is successful or not
     */
    boolean dispatch(CarbonMessage carbonMessage, CarbonCallback callback);

    String getProtocol();

}
