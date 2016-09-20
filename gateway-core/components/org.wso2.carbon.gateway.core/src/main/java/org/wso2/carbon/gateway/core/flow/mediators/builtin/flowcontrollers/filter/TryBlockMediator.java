package org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.exception.ChildExceptionHandler;
import org.wso2.carbon.gateway.core.exception.DefaultExceptionHandler;
import org.wso2.carbon.gateway.core.exception.FlowControllerExceptionCallback;
import org.wso2.carbon.gateway.core.flow.AbstractFlowController;
import org.wso2.carbon.gateway.core.flow.MediatorCollection;
import org.wso2.carbon.gateway.core.util.VariableUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Stack;

/**
 * Try block mediator
 */
public class TryBlockMediator extends AbstractFlowController {

    private static final Logger log = LoggerFactory.getLogger(TryBlockMediator.class);

    Stack<ChildExceptionHandler> exHandlers;
    private MediatorCollection childThenMediatorList = new MediatorCollection();

    public TryBlockMediator() {
        exHandlers = new Stack<>();
    }

    public void pushHandler(ChildExceptionHandler exceptionHandler) {
        exHandlers.push(exceptionHandler);
    }

    public boolean hasExceptionHandler () {
        return !exHandlers.isEmpty();
    }

    public ChildExceptionHandler popHandler() {
        return exHandlers.pop();
    }

    public boolean addHandler(ChildExceptionHandler childExceptionHandler) {
        return exHandlers.add(childExceptionHandler);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {

        childThenMediatorList.getFirstMediator().
                receive(carbonMessage, new FlowControllerExceptionCallback(carbonCallback, this,
                        VariableUtil.getVariableStack(carbonMessage), new DefaultExceptionHandler()));

        return true;
    }
}
