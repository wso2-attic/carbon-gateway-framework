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

package org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext;

import org.wso2.carbon.gateway.core.Constants;

/**
 * This is a registry for MessageBody Evaluators.
 */
public interface MessageBodyEvaluatorRegistry {
    /**
     * This adds a MessageBody Evaluator.
     * @param pathlanguage the path language eg:xpath,jsonpath
     * @param messageBodyEvaluator The MessageBodyEvaluator Instance to be added to the registry
     */
    void addMessageBodyEvaluator(Constants.PATHLANGUAGE pathlanguage, MessageBodyEvaluator messageBodyEvaluator);

    /**
     * Removes a MessageBodyEvaluator from registry.
     * @param pathlanguage the path language of the MessageBodyEvaluator to be removed
     * @return Should return a boolean depending on the success/failure of removal
     */
    boolean removeMessageBodyEvaluator(Constants.PATHLANGUAGE pathlanguage);

    /**
     * Rrtrieves the MessageBodyEvaluator for the specified path language.
     * @param pathlanguage the pathlanguage of the MessageBodyEvaluator to be retrieved
     * @return Returns the MessageBodyEvaluator instance
     */
    MessageBodyEvaluator getMessageBodyEvaluator(Constants.PATHLANGUAGE pathlanguage);
}
