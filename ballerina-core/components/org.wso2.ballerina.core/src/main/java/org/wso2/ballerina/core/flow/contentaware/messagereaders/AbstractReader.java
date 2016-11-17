/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.flow.contentaware.messagereaders;

import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageDataSource;

/**
 * An abstract class for Builders
 */
public abstract class AbstractReader implements Reader {

    protected String contentType;

    public AbstractReader(String contentType) {
        this.contentType = contentType;
    }

    public void attachMessageDataSource(MessageDataSource messageDataSource, CarbonMessage carbonMessage) {
        carbonMessage.setMessageDataSource(messageDataSource);
    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
