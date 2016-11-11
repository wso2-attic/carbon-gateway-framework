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
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.MessageBodyEvaluator;

import java.util.HashMap;
import java.util.Map;

/**
 * Base implementation of a MessageBodyEvaluatorRegistry.
 */
public class MessageBodyEvaluatorRegistry {
    private static final Logger log = LoggerFactory.getLogger(MessageBodyEvaluatorRegistry.class);

    /**
     * This is a singleton class and we are keeping an instance of BaseMessageBodyEvaluatorRegistry which
     * holds all the registered message body evaluators
     */
    private static MessageBodyEvaluatorRegistry instance = new MessageBodyEvaluatorRegistry();

    protected final Map<String, MessageBodyEvaluator> messageBodyEvaluators = new HashMap<>();

    private MessageBodyEvaluatorRegistry() {}

    public static MessageBodyEvaluatorRegistry getInstance() {
        return instance;
    }

    public void addMessageBodyEvaluator(String pathLanguage,
            MessageBodyEvaluator messageBodyEvaluator) {
        if (log.isDebugEnabled()) {
            log.debug("Adding message body evaluator: {}", messageBodyEvaluator);
        }
        messageBodyEvaluators.put(pathLanguage, messageBodyEvaluator);
    }

    public boolean removeMessageBodyEvaluator(String pathLanguage) {
        if (log.isDebugEnabled()) {
            log.debug("Removing message body evaluator of: {}", pathLanguage);
        }
        MessageBodyEvaluator messageBodyEvaluator = messageBodyEvaluators.remove(pathLanguage);
        return messageBodyEvaluator != null;
    }

    public MessageBodyEvaluator getMessageBodyEvaluator(String pathLanguage) {
        return messageBodyEvaluators.get(pathLanguage);
    }
}
