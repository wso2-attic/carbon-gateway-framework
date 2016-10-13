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

package org.wso2.carbon.gateway.evaluators.xpathevaluator;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.MessageBodyEvaluator;
import org.wso2.carbon.gateway.core.flow.contentaware.exceptions.MessageBodyEvaluationException;

import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;

/**
 * This class evaluates xpath expressions.
 */
public class XPathEvaluator implements MessageBodyEvaluator {
    private XPathCompiler xPathCompiler;
    private DocumentBuilder builder;

    public XPathEvaluator() {
        Processor processor = new Processor(false);
        xPathCompiler = processor.newXPathCompiler();
        builder = processor.newDocumentBuilder();
    }

    public Object evaluate(Object omElementObject, String xpathExpression)
            throws MessageBodyEvaluationException {
        if (omElementObject instanceof OMElement) {
            try {
                OMElement omElement = (OMElement) omElementObject;
                StringReader reader = new StringReader(omElement.toString());
                XdmNode doc = builder.build(new StreamSource(reader));
                XPathSelector selector = xPathCompiler.compile(xpathExpression).load();
                selector.setContextItem(doc);
                return selector.evaluate();
            } catch (SaxonApiException e) {
                throw new MessageBodyEvaluationException(e);
            }
        } else {
            throw new MessageBodyEvaluationException("The type " + omElementObject.getClass() + "is not supported");
        }
    }

    public Constants.PATHLANGUAGE getPathLanguage() {
        return Constants.PATHLANGUAGE.XPATH;
    }
}
