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
import org.wso2.carbon.gateway.core.flow.FlowControllerMediateCallback;
import org.wso2.carbon.gateway.core.flow.Mediator;
import org.wso2.carbon.gateway.core.flow.MediatorCollection;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.evaluator.Evaluator;
import org.wso2.carbon.gateway.core.util.VariableUtil;
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

    private String messageRef;

    public FilterMediator() {};

    public FilterMediator(Condition condition, String messageRef) {
        this.condition = condition;
        this.source = condition.getSource();
        this.pattern = condition.getPattern();
        this.messageRef = messageRef;
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

        Object referredCMsg = getObjectFromContext(carbonMessage, messageRef);
        // if the messageRef is not found as a CarbonMessage skip the filter mediator
        if (!(referredCMsg instanceof CarbonMessage)) {
            return next(carbonMessage, carbonCallback);
        }

        if (source.getScope().equals(Scope.HEADER)) {
            if (Evaluator.isHeaderMatched((CarbonMessage) referredCMsg, source, pattern)) {
                if (!(childThenMediatorList.getMediators().isEmpty())) {
                    super.receive(carbonMessage, carbonCallback);
                    childThenMediatorList.getFirstMediator().
                            receive(carbonMessage, new FlowControllerMediateCallback(carbonCallback, this,
                                    VariableUtil.getVariableStack(carbonMessage)));
                } else {
                    next(carbonMessage, carbonCallback);
                }
            } else {
                if (!(childOtherwiseMediatorList.getMediators().isEmpty())) {
                    super.receive(carbonMessage, carbonCallback);
                    childOtherwiseMediatorList.getFirstMediator().
                            receive(carbonMessage, new FlowControllerMediateCallback(carbonCallback, this,
                                    VariableUtil.getVariableStack(carbonMessage)));
                } else {
                   next(carbonMessage, carbonCallback);
                }
            }
        }

        return true;
    }

    public MediatorCollection getChildThenMediatorList () {
        return childThenMediatorList;
    }

    public MediatorCollection getChildOtherwiseMediatorList () {
        return childOtherwiseMediatorList;
    }

}
