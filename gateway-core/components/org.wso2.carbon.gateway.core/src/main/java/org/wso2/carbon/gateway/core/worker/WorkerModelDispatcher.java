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

package org.wso2.carbon.gateway.core.worker;

import com.lmax.disruptor.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.Mediator;
import org.wso2.carbon.gateway.core.flow.MediatorType;
import org.wso2.carbon.gateway.core.worker.disruptor.config.DisruptorManager;
import org.wso2.carbon.gateway.core.worker.disruptor.publisher.CarbonEventPublisher;
import org.wso2.carbon.gateway.core.worker.threadpool.PoolWorker;
import org.wso2.carbon.gateway.core.worker.threadpool.ThreadPoolFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.concurrent.ExecutorService;

/**
 * A class which can be used to get the underlying thread model.It can
 * be Disruptor model or Executor Service.
 */
public class WorkerModelDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(WorkerModelDispatcher.class);

    private static final WorkerModelDispatcher WORKER_MODEL_DISPATCHER = new WorkerModelDispatcher();

    private WorkerModelDispatcher() {

    }

    /**
     * Return WorkerModelDispatcher
     * @return WorkerModelDispatcher
     */
    public static WorkerModelDispatcher getInstance() {
        return WORKER_MODEL_DISPATCHER;
    }

    /**
     * Use for receive message from MessageProcessor
     *
     * @param carbonMessage
     * @param carbonCallback
     * @param workerMode
     * @return
     */
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback, MediatorType workerMode) {

        if (!ThreadPoolFactory.getInstance().isThreadPoolingEnable()) {
            // If Disruptor Mode is enabled
            if (workerMode == MediatorType.CPU_BOUND) {
                RingBuffer ringBuffer = DisruptorManager.getDisruptorConfig(DisruptorManager.DisruptorType.CPU_INBOUND)
                        .getDisruptor();
                ringBuffer.publishEvent(new CarbonEventPublisher(carbonMessage, null, carbonCallback));
            } else if (workerMode == MediatorType.IO_BOUND) {
                RingBuffer ringBuffer = DisruptorManager.getDisruptorConfig(DisruptorManager.DisruptorType.IO_INBOUND)
                        .getDisruptor();
                ringBuffer.publishEvent(new CarbonEventPublisher(carbonMessage, null, carbonCallback));
            }
        } else if (!(carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION) != null && carbonMessage
                .getProperty(org.wso2.carbon.messaging.Constants.DIRECTION).
                        equals(org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE))) {
            // If Diruptor mode is disabled and response message
            ExecutorService executorService = ThreadPoolFactory.getInstance().getInboundExecutorService();
            executorService.execute(new PoolWorker(carbonMessage, carbonCallback));
        } else {
            // Request message when Disruptor is disabled
            ExecutorService executorService = ThreadPoolFactory.getInstance().getOutboundExecutorService();
            executorService.execute(new PoolWorker(carbonMessage, carbonCallback));
        }

        return false;

    }

    /**
     * Use for dispatch messages in mediator level.If parent type is equal to current mediator type then do not
     * dispatch use same thread otherwise switch Disruptors
     * @param carbonMessage
     * @param carbonCallback
     * @param mediator
     * @param mediatorType
     * @return successfully handover to disruptor
     */
    public boolean dispatch(CarbonMessage carbonMessage, CarbonCallback carbonCallback, Mediator mediator,
            MediatorType mediatorType) {

        if (mediatorType == MediatorType.CPU_BOUND) {
            carbonMessage.setProperty(Constants.PARENT_TYPE, Constants.CPU_BOUND);
            RingBuffer ringBuffer = DisruptorManager.getDisruptorConfig(DisruptorManager.DisruptorType.CPU_INBOUND)
                    .getDisruptor();
            ringBuffer.publishEvent(new CarbonEventPublisher(carbonMessage, mediator, carbonCallback));
        } else if (mediatorType == MediatorType.IO_BOUND) {
            carbonMessage.setProperty(Constants.PARENT_TYPE, Constants.IO_BOUND);
            RingBuffer ringBuffer = DisruptorManager.getDisruptorConfig(DisruptorManager.DisruptorType.IO_INBOUND)
                    .getDisruptor();
            ringBuffer.publishEvent(new CarbonEventPublisher(carbonMessage, mediator, carbonCallback));
        }

        return true;
    }

}
