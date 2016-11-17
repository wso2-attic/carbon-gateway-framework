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

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.worker.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents the disruptor configuration.
 */
public class DisruptorConfig {

    private static Logger logger = LoggerFactory.getLogger(DisruptorConfig.class);

    //ring buffer size of Disruptor
    private int bufferSize = 512;
    //Default no of Disruptors if config not found this will start five Disruptors
    private int noDisruptors = 5;

    //No of event handlers per Disruptor.Event handler is working thread
    private int noOfEventHandlersPerDisruptor = 1;

    //default wait strategy of Disruptor and it is phase Spins, then yields, then waits
    //This strategy is used because it does not over or under consume CPU and resources.
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

    /**
     * provide ring buffer size of the Disruptor
     *
     * @return bufferSize
     */
    protected int getBufferSize() {
        return bufferSize;
    }

    /**
     * provide no of Disruptors to be configured
     *
     * @return noDisruptors
     */
    protected int getNoDisruptors() {
        return noDisruptors;
    }

    /**
     * provide no of EventHandlers per Disruptor
     *
     * @return noOfEventHandlersPerDisruptor
     */
    protected int getNoOfEventHandlersPerDisruptor() {
        return noOfEventHandlersPerDisruptor;
    }

    /**
     * wait strategy of Disruptor
     *
     * @return disruptorWaitStrategy e.g (BusySpin)
     */
    protected String getDisruptorWaitStrategy() {
        return disruptorWaitStrategy;
    }

    /**
     * provide a disruptor in round robin manner
     *
     * @return RingBuffer
     */
    public RingBuffer getDisruptor() {
        int ind = index.getAndIncrement() % noDisruptors;
        return ringBuffers.get(ind);
    }

    /**
     * Adding Disruptor to available Disruptor List
     *
     * @param disruptor
     */
    protected void addDisruptor(Disruptor disruptor) {
        disruptors.add(disruptor);
        ringBuffers.add(disruptor.start());
    }

    protected void notifyChannelInactive() {
        index.getAndDecrement();
    }

    protected void shutdownAllDisruptors() {
        for (Disruptor disruptor : disruptors) {
            disruptor.shutdown();
        }
        ringBuffers.clear();
    }

    protected void startAllDisruptors() {
        for (Disruptor disruptor : disruptors) {
            ringBuffers.add(disruptor.start());
        }
    }

    /**
     * Set Ringbuffer size of a Disruptor
     * @param bufferSize
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Set no of Disruptors
     * @param noDisruptors
     */
    public void setNoDisruptors(int noDisruptors) {
        this.noDisruptors = noDisruptors;
    }

    /**
     * set no of EventHandlers per Disruptor
     * @param noOfEventHandlersPerDisruptor
     */
    public void setNoOfEventHandlersPerDisruptor(int noOfEventHandlersPerDisruptor) {
        this.noOfEventHandlersPerDisruptor = noOfEventHandlersPerDisruptor;
    }

    /**
     * set wait strategy of a Disruptor
     * @param disruptorWaitStrategy
     */
    public void setDisruptorWaitStrategy(String disruptorWaitStrategy) {
        this.disruptorWaitStrategy = disruptorWaitStrategy;
    }
}
