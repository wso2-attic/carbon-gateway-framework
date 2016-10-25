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

package org.wso2.carbon.gateway.core.flow.contentaware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.MessageBodyEvaluator;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.MessageBodyEvaluatorRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Base implementation of a MessageBodyEvaluatorRegistry.
 */
public class BaseMessageBodyEvaluatorRegistry implements MessageBodyEvaluatorRegistry {
    private static final Logger log = LoggerFactory.getLogger(BaseMessageBodyEvaluatorRegistry.class);

    private static BaseMessageBodyEvaluatorRegistry instance = new BaseMessageBodyEvaluatorRegistry();

    protected final Map<Constants.PATHLANGUAGE, MessageBodyEvaluator> messageBodyEvaluators = new HashMap<>();

    private BaseMessageBodyEvaluatorRegistry() {}

    public static BaseMessageBodyEvaluatorRegistry getInstance() {
        return instance;
    }

    @Override
    public void addMessageBodyEvaluator(Constants.PATHLANGUAGE pathlanguage,
            MessageBodyEvaluator messageBodyEvaluator) {
        if (log.isDebugEnabled()) {
            log.debug("Adding message body evaluator: {}", messageBodyEvaluator);
        }
        messageBodyEvaluators.put(pathlanguage, messageBodyEvaluator);
    }

    @Override
    public boolean removeMessageBodyEvaluator(Constants.PATHLANGUAGE pathlanguage) {
        if (log.isDebugEnabled()) {
            log.debug("Removing message body evaluator of: {}", pathlanguage);
        }
        MessageBodyEvaluator messageBodyEvaluator = messageBodyEvaluators.remove(pathlanguage);
        return messageBodyEvaluator != null;
    }

    @Override
    public MessageBodyEvaluator getMessageBodyEvaluator(Constants.PATHLANGUAGE pathlanguage) {
        return messageBodyEvaluators.get(pathlanguage);
    }
}
