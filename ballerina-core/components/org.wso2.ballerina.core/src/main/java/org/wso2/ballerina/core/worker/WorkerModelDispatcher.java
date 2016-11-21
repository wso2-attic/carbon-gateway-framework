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

package org.wso2.ballerina.core.worker;

import com.lmax.disruptor.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.flow.Mediator;
import org.wso2.ballerina.core.flow.MediatorExecutionType;
import org.wso2.ballerina.core.worker.disruptor.config.DisruptorManager;
import org.wso2.ballerina.core.worker.disruptor.publisher.CarbonEventPublisher;
import org.wso2.ballerina.core.worker.threadpool.PoolWorker;
import org.wso2.ballerina.core.worker.threadpool.ThreadPoolFactory;
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
     * @return
     */
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {

        if (ThreadPoolFactory.getInstance().isThreadPoolingEnable()) { // When Thread Pool is enabled

            ExecutorService executorService;

            if (org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE.
                    equals(carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION))) { // Response Path
                executorService = ThreadPoolFactory.getInstance().getResponseFlowExecutorService();
            } else {
                executorService = ThreadPoolFactory.getInstance().getRequestFlowExecutorService();
            }
            executorService.execute(new PoolWorker(carbonMessage, carbonCallback));
        } else {    // When Disruptor is enabled
            RingBuffer ringBuffer = DisruptorManager.getDisruptorConfig(DisruptorManager.DisruptorType.CPU_BOUND)
                    .getDisruptor();
            ringBuffer.publishEvent(new CarbonEventPublisher(carbonMessage, null, carbonCallback));
        }

        return false;

    }

    /**
     * Switch Disruptor in mediator level.
     * If parent type is equal to current mediator execution type, then do not
     * switch. Otherwise switch the Disruptor
     *
     * @param carbonMessage         Carbon Message
     * @param carbonCallback        Callback
     * @param mediator              Mediator
     * @param mediatorExecutionType Target disruptor type
     * @return whether disruptor is switched successfully
     */
    public boolean switchDisruptor(CarbonMessage carbonMessage, CarbonCallback carbonCallback, Mediator mediator,
                                   MediatorExecutionType mediatorExecutionType) {

        // Safety check to avoid this happening when WorkerPool is enabled. TODO: we may remove this later
        if (ThreadPoolFactory.getInstance().isThreadPoolingEnable()) {
            return false;
        }

        RingBuffer ringBuffer;

        if (mediatorExecutionType == MediatorExecutionType.IO_BOUND) {
            carbonMessage.setProperty(Constants.PARENT_DISRUPTOR_TYPE, Constants.IO_BOUND);
            ringBuffer = DisruptorManager.getDisruptorConfig(DisruptorManager.DisruptorType.IO_BOUND).getDisruptor();
        } else {
            carbonMessage.setProperty(Constants.PARENT_DISRUPTOR_TYPE, Constants.CPU_BOUND);
            ringBuffer = DisruptorManager.getDisruptorConfig(DisruptorManager.DisruptorType.CPU_BOUND).getDisruptor();
        }
        ringBuffer.publishEvent(new CarbonEventPublisher(carbonMessage, mediator, carbonCallback));

        return true;
    }

}
