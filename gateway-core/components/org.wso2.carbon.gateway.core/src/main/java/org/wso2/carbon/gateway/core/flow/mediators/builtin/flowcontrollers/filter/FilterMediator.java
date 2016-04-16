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

package org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.AbstractFlowController;
import org.wso2.carbon.gateway.core.flow.FlowControllerCallback;
import org.wso2.carbon.gateway.core.flow.Mediator;
import org.wso2.carbon.gateway.core.flow.MediatorCollection;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.evaluator.Evaluator;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.regex.Pattern;

/**
 * Filter Mediator
 */
public class FilterMediator extends AbstractFlowController {

    private static final Logger log = LoggerFactory.getLogger(FilterMediator.class);

    private MediatorCollection childThenMediatorList = new MediatorCollection();
    private MediatorCollection childOtherwiseMediatorList = new MediatorCollection();

    private Source source;

    private Pattern pattern;

    private Condition condition;

    public FilterMediator() {};

    public FilterMediator(Condition condition) {
        this.condition = condition;
        this.source = condition.getSource();
        this.pattern = condition.getPattern();
    }

    public FilterMediator(Source source, Pattern pattern) {
        this.source = source;
        this.pattern = pattern;
    }

    public FilterMediator addthenMediators(MediatorCollection mediatorCollection) {
        childThenMediatorList = mediatorCollection;
        return this;
    }

    public FilterMediator addotherwiseMediators(MediatorCollection mediatorCollection) {
        childOtherwiseMediatorList = mediatorCollection;
        return this;
    }

    public void addThenMediator(Mediator mediator) {
        childThenMediatorList.addMediator(mediator);
    }

    public void addOtherwiseMediator(Mediator mediator) {
        childOtherwiseMediatorList.addMediator(mediator);
    }


    @Override
    public String getName() {
        return "filter";
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback)
               throws Exception {

        super.receive(carbonMessage, carbonCallback);

        if (source.getScope().equals(Scope.HEADER)) {

            if (Evaluator.isHeaderMatched(carbonMessage, source, pattern)) {
                childThenMediatorList.getFirstMediator().
                           receive(carbonMessage, new FlowControllerCallback(carbonCallback, this));
            } else {
                childOtherwiseMediatorList.getFirstMediator().
                           receive(carbonMessage, new FlowControllerCallback(carbonCallback, this));
            }
        }

        return true;
    }

}
