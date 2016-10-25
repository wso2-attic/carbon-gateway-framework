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
import org.wso2.carbon.gateway.core.flow.contentaware.exceptions.MessageBodyEvaluationException;

/**
 * Any MessageBodyEvaluator such as XPathEvaluator should implement this interface.
 */
public interface MessageBodyEvaluator {
    /**
     * Evaluates a message given an Path language.
     *
     * @param message message to be evaluated
     * @return
     */
    Object evaluate(Object message, String expression) throws MessageBodyEvaluationException;

    /**
     * Returns the relevant path language of the MessageBody evaluator.
     *
     * @return Path Language eg: xpath
     */
    Constants.PATHLANGUAGE getPathLanguage();
}
