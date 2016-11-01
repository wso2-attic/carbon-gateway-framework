/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.gateway.core.worker.disruptor.handler;

import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.Mediator;
import org.wso2.carbon.gateway.core.worker.Constants;
import org.wso2.carbon.gateway.core.worker.WorkerUtil;
import org.wso2.carbon.gateway.core.worker.disruptor.event.CarbonDisruptorEvent;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.concurrent.locks.Lock;

/**
 * Event Consumer of the Disruptor.
 */
public class CarbonDisruptorEventHandler implements EventHandler<CarbonDisruptorEvent> {

    private static final Logger logger = LoggerFactory.getLogger(CarbonDisruptorEventHandler.class);

    public CarbonDisruptorEventHandler() {
    }

    @Override
    public void onEvent(CarbonDisruptorEvent carbonDisruptorEvent, long sequence, boolean endOfBatch) throws Exception {

        CarbonMessage carbonMessage = (CarbonMessage) carbonDisruptorEvent.getEvent();
        Lock lock = carbonMessage.getLock();
        if (lock.tryLock()) {
            Object obj = carbonMessage.getProperty(Constants.PARENT_TYPE);
            CarbonCallback carbonCallback = carbonDisruptorEvent.getCarbonCallback();
            Object dir = carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION);
            // If response then call carbonCallback
            if (dir != null && dir.equals(org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE)) {
                carbonCallback.done(carbonMessage);

            } else {
                if (obj != null) {
                    //request received from mediator  and forward request to next mediator
                    Mediator mediator = carbonDisruptorEvent.getMediator();
                    mediator.receive(carbonMessage, carbonCallback);
                } else {
                    //request received from message processor
                    carbonMessage.setProperty(Constants.PARENT_TYPE, Constants.CPU_BOUND);
                    WorkerUtil.dispatchToInboundEndpoint(carbonMessage, carbonCallback);
                }
            }
        }

    }

}

