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

package org.wso2.carbon.gateway.evaluators.jsonpathevaluator;

import com.jayway.jsonpath.JsonPath;
import org.wso2.carbon.gateway.core.flow.contentaware.MIMEType;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.MessageBodyEvaluator;
import org.wso2.carbon.gateway.core.flow.contentaware.exceptions.MessageBodyEvaluationException;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class can be used to evaluate an JSON message against a given JSONPath expression.
 * This implements,
 * @see org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.MessageBodyEvaluator
 * which contains the method definitions for the functionality expected from a class that supports
 * evaluating a message body
 */
public class JSONPathEvaluator implements MessageBodyEvaluator {

    /**
     * This evaluates a JSON message against a provided JSONPath expression.
     *
     * @param inputStreamObject JSON input stream
     * @param expression JSON expression to evaluate the JSON message against
     * @return The resulting value
     * @throws MessageBodyEvaluationException
     */
    @Override
    public Object evaluate(Object inputStreamObject, String expression) throws MessageBodyEvaluationException {
        JsonPath jsonPath = JsonPath.compile(expression);
        try {
            if (inputStreamObject instanceof InputStream) {
                return jsonPath.read((InputStream) inputStreamObject);
            } else {
                throw new MessageBodyEvaluationException("The type " + inputStreamObject.getClass()
                        + "is not supported");
            }
        } catch (IOException e) {
            throw new MessageBodyEvaluationException(e);
        }
    }

    /**
     * Returns the path language of this evaluator.
     *
     * @return enum for JSONPath
     */
    @Override
    public String getPathLanguage() {
        return "jsonpath";
    }

    /**
     * Returns a boolean depending on whether the provided mime type is supported in this Path Evaluator
     *
     * @param mimeType The mime type that need to be checked if it is supported
     * @return Whether the mime type is supported
     */
    @Override
    public Boolean isContentTypeSupported(String mimeType) {
        return MIMEType.JSON.equals(mimeType);
    }
}
