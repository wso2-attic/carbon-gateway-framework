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

package org.wso2.ballerina.core.flow.contentaware;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.flow.contentaware.abstractcontext.MessageBodyEvaluator;

/**
 * This osgi service component is responsible for adding MessageBodyEvaluator services to the
 * BaseMessageBodyEvaluatorRegistry when they become available and remove when they become unavailable.
 */
@Component(
        name = "org.wso2.ballerina.core.flow.contentAwareSupport.MessageEvaluatorServiceComponent",
        immediate = true)
public class MessageBodyEvaluatorServiceComponent {
    private final Logger log = LoggerFactory.getLogger(MessageBodyEvaluatorServiceComponent.class);

    /**
     * Adds a MessageBodyEvaluator to BaseMessageBodyEvaluatorRegistry.
     *
     * @param messageBodyEvaluator MessageBodyEvaluator to be added
     */
    @Reference(
            name = "MessageBodyEvaluator-Service",
            service = MessageBodyEvaluator.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeMessageBodyEvaluator")
    protected void addMessageBodyEvaluator(MessageBodyEvaluator messageBodyEvaluator) {
        if (log.isDebugEnabled()) {
            log.debug("Adding" + messageBodyEvaluator.toString() + "to MessageBodyEvaluatorRegistry");
        }
        MessageBodyEvaluatorRegistry.getInstance()
                .addMessageBodyEvaluator(messageBodyEvaluator.getPathLanguage(), messageBodyEvaluator);
    }

    /**
     * Removes a MessageBodyEvaluator from BaseMessageBodyEvaluatorRegistry.
     *
     * @param messageBodyEvaluator MessageBodyEvaluator to be removed
     */
    protected void removeMessageBodyEvaluator(MessageBodyEvaluator messageBodyEvaluator) {
        if (log.isDebugEnabled()) {
            log.debug("Removing" + messageBodyEvaluator.toString() + "from MessageBodyEvaluatorRegistry");
        }
        MessageBodyEvaluatorRegistry.getInstance()
                .removeMessageBodyEvaluator(messageBodyEvaluator.getPathLanguage());
    }

}
