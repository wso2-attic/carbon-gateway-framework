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
package org.wso2.ballerina.core.flow.mediators.builtin.manipulators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.flow.AbstractMediator;
import org.wso2.ballerina.core.flow.MediatorType;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Basic Enrich mediator implementation
 * TODO: Not implemented yet
 */
public class EnrichMediator extends AbstractMediator {
    private static final Logger log = LoggerFactory.getLogger(EnrichMediator.class);

    @Override
    public String getName() {
        return "enrich";
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        log.info("Message is received at Enrich mediator");
        return next(carbonMessage, carbonCallback);
    }

    @Override
    public MediatorType getMediatorType() {
        return MediatorType.CPU_BOUND;
    }
}
