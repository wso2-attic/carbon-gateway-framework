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

package org.wso2.carbon.gateway.core.worker.config;

import org.wso2.carbon.gateway.core.worker.Constants;
import org.wso2.carbon.gateway.core.worker.disruptor.config.DisruptorConfig;
import org.wso2.carbon.gateway.core.worker.disruptor.config.DisruptorManager;
import org.wso2.carbon.gateway.core.worker.threadpool.ThreadPoolFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * JAXB representation of the Engine Thread Model.
 */
@SuppressWarnings("unused")
@XmlRootElement(name = "engine")
@XmlAccessorType(XmlAccessType.FIELD)
public class ThreadModelConfiguration {


    public static ThreadModelConfiguration getDefault() {
        ThreadModelConfiguration defaultConfig = new ThreadModelConfiguration();
        DisruptorConfiguration disruptorConfiguration = DisruptorConfiguration.getDefault();
        HashSet<DisruptorConfiguration> disruptorConfigurations = new HashSet<>();
        disruptorConfigurations.add(disruptorConfiguration);
        defaultConfig.setDisruptorConfigurations(disruptorConfigurations);
        ThreadPoolConfiguration threadPoolConfiguration = ThreadPoolConfiguration.getDefault();
        Set<ThreadPoolConfiguration> threadPoolConfigurations1 = new HashSet<>();
        threadPoolConfigurations1.add(threadPoolConfiguration);
        defaultConfig.setThreadPoolConfigurations(threadPoolConfigurations1);
        return defaultConfig;
    }

    @XmlElementWrapper(name = "disruptorConfigurations")
    @XmlElement(name = "disruptorConfiguration")
    private Set<DisruptorConfiguration> disruptorConfigurations;

    @XmlElementWrapper(name = "threadPoolConfigurations")
    @XmlElement(name = "threadPoolConfiguration")
    private Set<ThreadPoolConfiguration> threadPoolConfigurations;


    public Set<DisruptorConfiguration> getDisruptorConfigurations() {
        if (disruptorConfigurations == null) {
            return Collections.EMPTY_SET;
        }
        return Collections.unmodifiableSet(disruptorConfigurations);
    }

    public void setDisruptorConfigurations(Set<DisruptorConfiguration> disruptorConfigurations) {
        this.disruptorConfigurations = Collections.unmodifiableSet(disruptorConfigurations);
    }

    public Set<ThreadPoolConfiguration> getThreadPoolConfigurations() {
        if (threadPoolConfigurations == null) {
            return Collections.EMPTY_SET;
        }
        return Collections.unmodifiableSet(threadPoolConfigurations);
    }

    public void setThreadPoolConfigurations(Set<ThreadPoolConfiguration> threadPoolConfigurations) {
        this.threadPoolConfigurations = Collections.unmodifiableSet(threadPoolConfigurations);
    }

    public void configure() {

        Iterator iterator = threadPoolConfigurations.iterator();
        if (iterator.hasNext()) {
            ThreadPoolConfiguration threadModelConfiguration = (ThreadPoolConfiguration) iterator.next();
            ThreadPoolFactory.getInstance().createThreadPool
                       (threadModelConfiguration.getNoOfThreads());
            ThreadPoolFactory.getInstance().setThreadPoolingEnable(threadModelConfiguration.isEnable());
        }

        for (DisruptorConfiguration disruptorConfiguration : disruptorConfigurations) {
            String id = disruptorConfiguration.getId();
            if (id.equals(Constants.CPU_BOUND)) {
                DisruptorConfig disruptorConfig = new DisruptorConfig();
                List<Parameter> parameterList = disruptorConfiguration.getParameters();
                for (Parameter parameter : parameterList) {
                    if (parameter.getName().equals(Constants.DISRUPTOR_BUFFER_SIZE)) {
                        disruptorConfig.setBufferSize(Integer.parseInt(parameter.getValue()));
                    } else if (parameter.getName().equals(Constants.DISRUPTOR_COUNT)) {
                        disruptorConfig.setNoDisruptors(Integer.parseInt(parameter.getValue()));
                    } else if (parameter.getName().equals(Constants.DISRUPTOR_EVENT_HANDLER_COUNT)) {
                        disruptorConfig.setNoOfEventHandlersPerDisruptor(Integer.parseInt(parameter.getValue()));
                    } else if (parameter.getName().equals(Constants.WAIT_STRATEGY)) {
                        disruptorConfig.setDisruptorWaitStrategy(parameter.getValue());
                    }
                }
                DisruptorManager.createDisruptors(DisruptorManager.DisruptorType.CPU_INBOUND, disruptorConfig);
            } else if (id.equals(Constants.IO_BOUND)) {
                DisruptorConfig disruptorConfig = new DisruptorConfig();
                List<Parameter> parameterList = disruptorConfiguration.getParameters();
                for (Parameter parameter : parameterList) {
                    if (parameter.getName().equals(Constants.DISRUPTOR_BUFFER_SIZE)) {
                        disruptorConfig.setBufferSize(Integer.parseInt(parameter.getValue()));
                    } else if (parameter.getName().equals(Constants.DISRUPTOR_COUNT)) {
                        disruptorConfig.setNoDisruptors(Integer.parseInt(parameter.getValue()));
                    } else if (parameter.getName().equals(Constants.DISRUPTOR_EVENT_HANDLER_COUNT)) {
                        disruptorConfig.setNoOfEventHandlersPerDisruptor(Integer.parseInt(parameter.getValue()));
                    } else if (parameter.getName().equals(Constants.WAIT_STRATEGY)) {
                        disruptorConfig.setDisruptorWaitStrategy(parameter.getValue());
                    }
                }
                DisruptorManager.createDisruptors(DisruptorManager.DisruptorType.IO_INBOUND, disruptorConfig);
            }


        }


    }
}
