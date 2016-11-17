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

package org.wso2.ballerina.core.worker.disruptor.config;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.PhasedBackoffWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.wso2.ballerina.core.worker.Constants;
import org.wso2.ballerina.core.worker.disruptor.event.CarbonDisruptorEvent;
import org.wso2.ballerina.core.worker.disruptor.exception.DisruptorExceptionHandler;
import org.wso2.ballerina.core.worker.disruptor.handler.CarbonDisruptorEventHandler;
import org.wso2.ballerina.core.worker.threadpool.ThreadPoolFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Util Class creates Disruptors for Inbound and Outbound Transports.
 */
public class DisruptorManager {

    private static ConcurrentHashMap<DisruptorType, DisruptorConfig> disruptorConfigHashMap =
               new ConcurrentHashMap<>();

    /**
     * create and configure Disruptors  using given Disruptor Configurations
     * @param type
     * @param disruptorConfig
     */
    @SuppressWarnings("unchecked")
    public static synchronized void createDisruptors(DisruptorType type, DisruptorConfig disruptorConfig) {
        if (!ThreadPoolFactory.getInstance().isThreadPoolingEnable()) {
            WaitStrategy inboundWaitStrategy = getWaitStrategy(disruptorConfig.getDisruptorWaitStrategy());
            for (int i = 0; i < disruptorConfig.getNoDisruptors(); i++) {
                ExecutorService executorService = Executors.newFixedThreadPool
                           (disruptorConfig.getNoOfEventHandlersPerDisruptor());

                Disruptor disruptor = new Disruptor<>(CarbonDisruptorEvent.EVENT_FACTORY,
                                                      disruptorConfig.getBufferSize(),
                                                      executorService,
                                                      ProducerType.MULTI,
                                                      inboundWaitStrategy);
                ExceptionHandler exh = new DisruptorExceptionHandler();


                EventHandler[] eventHandlers = new EventHandler[disruptorConfig.getNoOfEventHandlersPerDisruptor()];
                for (int j = 0; j < disruptorConfig.getNoOfEventHandlersPerDisruptor(); j++) {
                    EventHandler eventHandler = new CarbonDisruptorEventHandler();
                    eventHandlers[j] = eventHandler;
                }
                disruptor.handleEventsWith(eventHandlers);
                for (EventHandler eventHandler : eventHandlers) {
                    disruptor.handleExceptionsFor(eventHandler).with(exh);
                }
                disruptorConfig.addDisruptor(disruptor);
            }
            disruptorConfigHashMap.put(type, disruptorConfig);
        }
    }


    private static WaitStrategy getWaitStrategy(String waitstrategy) {
        WaitStrategy waitStrategy;
        switch (waitstrategy) {
            case Constants.BLOCKING_WAIT:
                waitStrategy = new BlockingWaitStrategy();
                break;
            case Constants.BUSY_SPIN:
                waitStrategy = new BusySpinWaitStrategy();
                break;
            case Constants.LITE_BLOCKING:
                waitStrategy = new LiteBlockingWaitStrategy();
                break;
            case Constants.SLEEP_WAITING:
                waitStrategy = new SleepingWaitStrategy();
                break;
            case Constants.TIME_BLOCKING:
                waitStrategy = new TimeoutBlockingWaitStrategy(1, TimeUnit.SECONDS);
                break;
            case Constants.YIELD_WAITING:
                waitStrategy = new YieldingWaitStrategy();
                break;
            default:
                waitStrategy = PhasedBackoffWaitStrategy.withLiteLock(1, 4, TimeUnit.SECONDS);

        }
        return waitStrategy;
    }


    public static DisruptorConfig getDisruptorConfig(DisruptorType disruptorType) {
        return disruptorConfigHashMap.get(disruptorType);
    }

    /**
     * Shutting down all Disruptors
     */
    public static synchronized void shutdownAllDisruptors() {
        for (Map.Entry entry : disruptorConfigHashMap.entrySet()) {
            DisruptorConfig disruptorConfig = (DisruptorConfig) entry.getValue();
            disruptorConfig.shutdownAllDisruptors();
        }
    }



    /**
     * Describe types of Disruptors.
     */
    public enum DisruptorType {
        CPU_INBOUND, IO_INBOUND
    }

}
