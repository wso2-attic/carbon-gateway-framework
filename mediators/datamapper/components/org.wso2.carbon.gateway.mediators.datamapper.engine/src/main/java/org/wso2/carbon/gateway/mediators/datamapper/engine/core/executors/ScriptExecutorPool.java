/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.gateway.mediators.datamapper.engine.core.executors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *  This class creates a pool of script executors for data mapper. 
 */
public class ScriptExecutorPool {

    private BlockingQueue<Executor> executors;

    /**
     * Creates a pool fo script executors.
     * 
     * @param executorType      Executor type
     * @param executorPoolSize  Pool size
     */
    public ScriptExecutorPool(ScriptExecutorType executorType, int executorPoolSize) {
        executors = new LinkedBlockingQueue<>();
        for (int i = 0; i < executorPoolSize; i++) {
            Executor executor = createScriptExecutor(executorType);
            executors.add(executor);
        }
    }

    private Executor createScriptExecutor(ScriptExecutorType executorType) {
        return new ScriptExecutor(executorType);
    }

    /**
     * Get an executor.
     * 
     * @return  Executor
     * @throws  InterruptedException
     */
    public Executor take() throws InterruptedException {
        return executors.take();
    }

    /**
     * Add an executor to the pool.
     * 
     * @param executor  Executor to be added to the pool
     * @throws          InterruptedException
     */
    public void put(Executor executor) throws InterruptedException {
        executors.put(executor);
    }
}
