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

package org.wso2.carbon.gateway.core.worker.disruptor.config;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.worker.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents the disruptor configuration.
 */
public class DisruptorConfig {

    private static Logger logger = LoggerFactory.getLogger(DisruptorConfig.class);

    private int bufferSize = 512;
    private int noDisruptors = 5;
    private int noOfEventHandlersPerDisruptor = 1;
    private String disruptorWaitStrategy = Constants.PHASED_BACKOFF;
    private List<RingBuffer> ringBuffers = new ArrayList<>();
    private List<Disruptor> disruptors = new ArrayList<>();
    private AtomicInteger index = new AtomicInteger(0);

    public DisruptorConfig() {

    }

    public DisruptorConfig(String bufferSize, String noDisruptors, String noOfEventHandlersPerDisruptor,
                           String disruptorWaitStrategy) {
        if (bufferSize != null) {
            this.bufferSize = Integer.parseInt(bufferSize);
        }

        if (noDisruptors != null) {
            this.noDisruptors = Integer.parseInt(noDisruptors);
        }

        if (noOfEventHandlersPerDisruptor != null) {
            this.noOfEventHandlersPerDisruptor = Integer.parseInt(noOfEventHandlersPerDisruptor);
        }

        if (disruptorWaitStrategy != null) {
            this.disruptorWaitStrategy = disruptorWaitStrategy;
        }

        logger.debug("Disruptor configration created with buffer size :=  " + this.bufferSize +
                     " , no of disruptors :=" + this.noDisruptors +
                     " , no of event handlers per disruptor := " + this.noOfEventHandlersPerDisruptor +
                     ", wait strategy :=" + this.disruptorWaitStrategy);
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getNoDisruptors() {
        return noDisruptors;
    }

    public int getNoOfEventHandlersPerDisruptor() {
        return noOfEventHandlersPerDisruptor;
    }

    public String getDisruptorWaitStrategy() {
        return disruptorWaitStrategy;
    }

    public RingBuffer getDisruptor() {
        int ind = index.getAndIncrement() % noDisruptors;
        return ringBuffers.get(ind);
    }

    public void addDisruptor(Disruptor disruptor) {
        disruptors.add(disruptor);
        ringBuffers.add(disruptor.start());
    }

    public void notifyChannelInactive() {
        index.getAndDecrement();
    }

    public  void shutdownAllDisruptors() {
        for (Disruptor disruptor : disruptors) {
            disruptor.shutdown();
        }
        ringBuffers.clear();
    }

    public  void startAllDisruptors() {
        for (Disruptor disruptor : disruptors) {
            ringBuffers.add(disruptor.start());
        }
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setNoDisruptors(int noDisruptors) {
        this.noDisruptors = noDisruptors;
    }

    public void setNoOfEventHandlersPerDisruptor(int noOfEventHandlersPerDisruptor) {
        this.noOfEventHandlersPerDisruptor = noOfEventHandlersPerDisruptor;
    }

    public void setDisruptorWaitStrategy(String disruptorWaitStrategy) {
        this.disruptorWaitStrategy = disruptorWaitStrategy;
    }
}
