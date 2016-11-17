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

package org.wso2.ballerina.evaluators.xpathevaluator;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import org.wso2.ballerina.core.flow.contentaware.MIMEType;
import org.wso2.ballerina.core.flow.contentaware.abstractcontext.MessageBodyEvaluator;
import org.wso2.ballerina.core.flow.contentaware.exceptions.MessageBodyEvaluationException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.stream.StreamSource;

/**
 * This class can be used to evaluate an XML message against a given XPath expression.
 * This implements,
 * @see org.wso2.ballerina.core.flow.contentaware.abstractcontext.MessageBodyEvaluator
 * which contains the method definitions for the functionality expected from a class that supports
 * evaluating a message body
 */
public class XPathEvaluator implements MessageBodyEvaluator {
    // An XPathCompiler object allows XPath queries to be compiled.
    private XPathCompiler xPathCompiler;

    // The Processor acts as a factory for generating XQuery, XPath, and XSLT compilers;
    // Once established, a Processor may be used in multiple threads.
    private Processor processor;

    public XPathEvaluator() {
        // The boolean argument indicates whether this is the licensed edition or not.
        processor = new Processor(false);
        xPathCompiler = processor.newXPathCompiler();
    }

    /**
     * This evaluates an XML message against a provided XPath expression.
     *
     * @param inputStream input stream to be evaluated
     * @param xpathExpression XPath expression to evaluate the OMElement against
     * @return The resulting value
     * @throws MessageBodyEvaluationException
     */
    @Override
    public Object evaluate(InputStream inputStream, String xpathExpression) throws MessageBodyEvaluationException {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            DocumentBuilder builder = processor.newDocumentBuilder();
            XdmNode doc = builder.build(new StreamSource(inputStreamReader));
            XPathSelector selector = xPathCompiler.compile(xpathExpression).load();
            selector.setContextItem(doc);
            return selector.evaluate();
        } catch (SaxonApiException e) {
            throw new MessageBodyEvaluationException("There is a problem evaluating the XPath", e);
        }
    }

    /**
     * Returns the path language of this evaluator.
     *
     * @return enum for XPATH
     */
    @Override
    public String getPathLanguage() {
        return "xpath";
    }

    /**
     * Returns a boolean depending on whether the provided mime type is supported in this Path Evaluator
     *
     * @param mimeType The mime type that need to be checked if it is supported
     * @return Whether the mime type is supported
     */
    @Override
    public Boolean isContentTypeSupported(String mimeType) {
        switch (mimeType) {
        case MIMEType.XML:
            return true;
        case MIMEType.TEXT_XML:
            return true;
        default:
            return false;
        }
    }
}
