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

package org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.contentaware.MessageBodyEvaluatorRegistry;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.MessageBodyEvaluator;
import org.wso2.carbon.gateway.core.flow.contentaware.exceptions.MessageBodyEvaluationException;
import org.wso2.carbon.gateway.core.flow.contentaware.messagereaders.Reader;
import org.wso2.carbon.gateway.core.flow.contentaware.messagereaders.ReaderRegistryImpl;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.Source;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageDataSource;

import java.util.regex.Pattern;

/**
 * A Util class responsible for evaluate carbon message according to condition
 */
public class Evaluator {
    private static final Logger log = LoggerFactory.getLogger(Evaluator.class);

    /**
     * Returns a boolean depending on whether a given header in the CarbonMessage matches with the provided pattern.
     *
     * @param carbonMessage The CarbonMessage of which the header needs to be evaluated
     * @param source Contains the matching condition
     * @param pattern The pattern to match the header against
     * @return Whether the pattern is matched or not
     */
    public static boolean isHeaderMatched(CarbonMessage carbonMessage, Source source, Pattern pattern) {
        if (carbonMessage.getHeaders().contains(source.getKey())) {
            return pattern.matcher(carbonMessage.getHeaders().get(source.getKey())).matches();
        }
        return false;
    }

    /**
     * Returns a boolean depending on whether the result of a given path language expression against message body
     * matches with the provided pattern.
     *
     * @param carbonMessage The CarbonMessage of which the message body needs to be evaluated
     * @param source Contains the matching condition
     * @param pattern The pattern to match the path language evaluation result against
     * @return Whether the pattern is matched or not
     * @throws Exception
     */
    public static boolean isPathMatched(CarbonMessage carbonMessage, Source source, Pattern pattern) throws Exception {
        MessageBodyEvaluator messageBodyEvaluator = MessageBodyEvaluatorRegistry.getInstance()
                .getMessageBodyEvaluator(source.getPathLanguage());
        Reader reader = ReaderRegistryImpl.getInstance().getReader(carbonMessage);
        String contentType = reader.getContentType();
        if (messageBodyEvaluator.isContentTypeSupported(contentType)) {
            MessageDataSource messageDataSource = carbonMessage.getMessageDataSource();
            Object dataObject = messageDataSource != null ? messageDataSource.getDataObject()
                    : reader.makeMessageReadable(carbonMessage).getDataObject();
            Object result = messageBodyEvaluator.evaluate(dataObject, source.getKey());
            return result != null && pattern.matcher(result.toString()).matches();
        } else {
            throw new MessageBodyEvaluationException(
                    messageBodyEvaluator.getPathLanguage() + " cannot be applied for a message body of the type "
                            + contentType);
        }
    }
}
