package org.wso2.carbon.gateway.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.FlowControllerCallback;
import org.wso2.carbon.gateway.core.flow.Mediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.TryBlockMediator;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Stack;

/**
 * Callback related to Exception handling Mediators
 */
public class FlowControllerExceptionCallback implements CarbonCallback {

    /* Incoming callback */
    CarbonCallback parentCallback;

    /* Flow Controller Mediator */
    Mediator mediator;

//    Stack<Map<String, Object>> variableStack;

    private static final Logger log = LoggerFactory.getLogger(FlowControllerCallback.class);

    public FlowControllerExceptionCallback(CarbonCallback parentCallback, Mediator mediator, Stack variableStack) {
        this.parentCallback = parentCallback;
        this.mediator = mediator;
//        this.variableStack = variableStack;
    }

    @Override
    public void done(CarbonMessage carbonMessage) {

        CustomException customException = (CustomException) carbonMessage.getProperty("Exception");

        // Child exception handler
        while (((TryBlockMediator) getMediator()).hasExceptionHandler()) {
            ChildExceptionHandler exHandler = ((TryBlockMediator) getMediator()).popHandler();

            if (exHandler.canHandle().isInstance(customException)) {
                exHandler.handleException(carbonMessage, parentCallback);

                if (mediator.hasNext()) {
                    // If Mediator has a sibling after this
                    try {
                        mediator.next(carbonMessage, parentCallback);
                    } catch (Exception e) {
                        log.error("Error while mediating from Callback", e);
                    }
                } else if (parentCallback instanceof FlowControllerCallback) {
                    //If no siblings handover message to the requester
                    parentCallback.done(carbonMessage);
                }

                return;
            }
        }

        // If no child handler, use the default exception handler
        new DefaultExceptionHandler().handleException(carbonMessage, parentCallback);
    }

    public Mediator getMediator() {
        return mediator;
    }
}
