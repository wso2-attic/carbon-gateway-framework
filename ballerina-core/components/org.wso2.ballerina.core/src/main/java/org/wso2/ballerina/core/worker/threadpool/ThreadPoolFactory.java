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

package org.wso2.ballerina.core.worker.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class which represents the Thread Pool configurations
 */
public class ThreadPoolFactory {

    private static final ThreadPoolFactory threadPoolFactory = new ThreadPoolFactory();

    //TODO: Make these configurable
    // handle requestFlowExecutorService requests
    private ExecutorService requestFlowExecutorService =  Executors.newFixedThreadPool(20);

    //handle responseFlowExecutorService requests
    private ExecutorService responseFlowExecutorService =  Executors.newFixedThreadPool(20);

    private boolean threadPoolingEnable = true;

    public void createThreadPool(int noOfThreads) {
        requestFlowExecutorService = Executors.newFixedThreadPool(noOfThreads);
        responseFlowExecutorService = Executors.newFixedThreadPool(noOfThreads);
    }

    public ExecutorService getRequestFlowExecutorService() {
        return requestFlowExecutorService;
    }

    public ExecutorService getResponseFlowExecutorService() {
        return responseFlowExecutorService;
    }

    public boolean isThreadPoolingEnable() {
        return threadPoolingEnable;
    }

    public void setThreadPoolingEnable(boolean threadPoolingEnable) {
        this.threadPoolingEnable = threadPoolingEnable;
    }

    private ThreadPoolFactory() {

    }

    public static ThreadPoolFactory getInstance() {
        return threadPoolFactory;
    }
}
