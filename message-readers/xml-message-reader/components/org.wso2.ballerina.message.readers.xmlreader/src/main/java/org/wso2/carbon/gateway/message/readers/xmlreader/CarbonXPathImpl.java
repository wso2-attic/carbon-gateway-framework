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

package org.wso2.ballerina.message.readers.xmlreader;

import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;

/**
 * A carbon xpath implementation
 */
public class CarbonXPathImpl extends AXIOMXPath {

    private static final long serialVersionUID = 763922613753433423L;

    private String expression;

    public CarbonXPathImpl(String expression) throws JaxenException {
        super(expression);
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public Object evaluate(Object context) throws JaxenException {
        return super.evaluate(context);
    }

}
