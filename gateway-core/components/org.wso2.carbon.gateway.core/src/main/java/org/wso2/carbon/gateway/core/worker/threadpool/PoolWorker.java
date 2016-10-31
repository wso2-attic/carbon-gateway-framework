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

package org.wso2.carbon.gateway.core.worker.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.worker.WorkerUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;

/**
 * A class which represents the Worker for ThreadPool
 */
public class PoolWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PoolWorker.class);

    private CarbonMessage carbonMessage;

    private CarbonCallback carbonCallback;

    public PoolWorker(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        this.carbonMessage = carbonMessage;
        this.carbonCallback = carbonCallback;

    }

    public void run() {

        Object dir = carbonMessage.getProperty(Constants.DIRECTION);
        try {
            if (dir != null && dir.equals(Constants.DIRECTION_RESPONSE)) {
                carbonCallback.done(carbonMessage);
            } else {
                WorkerUtil.dispatchToInboundEndpoint(carbonMessage, carbonCallback);
            }

        } catch (Exception e) {
            logger.error("Cannot process the message through Executor Service", e);
        }
    }
}
