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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.flow.contentaware.BaseMessageBodyEvaluatorRegistry;
import org.wso2.carbon.gateway.core.flow.contentaware.MIMEType;
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

    public static boolean isHeaderMatched(CarbonMessage carbonMessage, Source source, Pattern pattern) {
        if (carbonMessage.getHeaders().contains(source.getKey())) {
            return pattern.matcher(carbonMessage.getHeaders().get(source.getKey())).matches();
        }
        return false;
    }

    public static boolean isXPathMatched(CarbonMessage carbonMessage, Source source, Pattern pattern) throws Exception {
        Reader reader = ReaderRegistryImpl.getInstance().getReader(carbonMessage);
        MessageDataSource messageDataSource = reader.makeMessageReadable(carbonMessage);

        OMElement messageBody = ((SOAPEnvelope) messageDataSource.getDataObject()).getBody().getFirstElement();
        String contentType = reader.getContentType();
        Object xpathResult = null;

        switch (contentType) {
        case MIMEType.XML:
            xpathResult = evaluateXPath(messageBody, source.getKey());
            // TODO: Need a better way to do this.
            // Here I'm just converting the resulting XDMValue to string and compare
            // But the result of an xpath expression could be a boolean,
            // it could be a set of nodes, etc..
            return pattern.matcher(xpathResult.toString()).matches();
        //TODO: Add cases for other types as appropriate
        default: break;
        }

        return false;
    }

    private static Object evaluateXPath(OMElement messageBody, String xpathExpression) throws
            MessageBodyEvaluationException {
        MessageBodyEvaluator messageBodyEvaluator = BaseMessageBodyEvaluatorRegistry.getInstance()
                .getMessageBodyEvaluator(Constants.PATHLANGUAGE.XPATH);
        return messageBodyEvaluator.evaluate(messageBody, xpathExpression);
    }
}
