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
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.MessageBodyEvaluator;
import org.wso2.carbon.gateway.core.flow.contentaware.exceptions.MessageBodyEvaluationException;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class evaluates jsonpath expressions.
 */
public class JSONPathEvaluator implements MessageBodyEvaluator {

    @Override
    public Object evaluate(Object inputStreamObject, String expression) throws MessageBodyEvaluationException {
        JsonPath jsonPath = JsonPath.compile(expression);
        try {
            if (inputStreamObject instanceof InputStream) {
                Object result = jsonPath.read((InputStream) inputStreamObject);
                return result;
            } else {
                throw new MessageBodyEvaluationException("The type " + inputStreamObject.getClass()
                        + "is not supported");
            }
        } catch (IOException e) {
            throw new MessageBodyEvaluationException(e);
        }
    }

    @Override
    public Constants.PATHLANGUAGE getPathLanguage() {
        return Constants.PATHLANGUAGE.JSONPATH;
    }
}
