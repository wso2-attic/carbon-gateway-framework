/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.gateway.core.outbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageDataSource;

/**
 * Basic implementation for Outbound Endpoint
 */
public abstract class AbstractOutboundEndpoint implements OutboundEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOutboundEndpoint.class);

    private int timeOut;

    private String name;

    public AbstractOutboundEndpoint(String name) {
        this.name = name;
    }

    public AbstractOutboundEndpoint() {
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        if (carbonMessage.isAlreadyRead()) {
            MessageDataSource messageDataSource = carbonMessage.getMessageDataSource();
            if (messageDataSource != null) {
                messageDataSource.serializeData();
                carbonMessage.setEndOfMsgAdded(true);
                carbonMessage.getHeaders().remove(Constants.HTTP_CONTENT_LENGTH);
                carbonMessage.getHeaders()
                        .set(Constants.HTTP_CONTENT_LENGTH, String.valueOf(carbonMessage.getFullMessageLength()));

            } else {
                LOGGER.error("Message is already built but cannot find the MessageDataSource");
            }
        }
        return true;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
