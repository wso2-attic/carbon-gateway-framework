package org.wso2.carbon.gateway.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.FlowControllerCallback;
import org.wso2.carbon.gateway.core.flow.FlowControllerMediateCallback;
import org.wso2.carbon.gateway.core.flow.Mediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.TryBlockMediator;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.exceptions.NelException;

import java.util.Stack;

/**
 * Callback related to Exception handling Mediators
 */
public class FlowControllerExceptionCallback implements FlowControllerCallback {

    /* Incoming callback */
    CarbonCallback parentCallback;

    /* Flow Controller Mediator */
    Mediator mediator;

    DefaultExceptionHandler defaultExceptionHandler;

//    Stack<Map<String, Object>> variableStack;

    private static final Logger log = LoggerFactory.getLogger(FlowControllerMediateCallback.class);

    public FlowControllerExceptionCallback(CarbonCallback parentCallback, Mediator mediator,
                                           Stack variableStack, DefaultExceptionHandler defaultExceptionHandler) {
        this.parentCallback = parentCallback;
        this.mediator = mediator;
        this.defaultExceptionHandler = defaultExceptionHandler;
//        this.variableStack = variableStack;
    }

    @Override
    public void done(CarbonMessage carbonMessage) {

        if (canProcess(carbonMessage)) {

            NelException customException = carbonMessage.getNelException();
            // Child exception handler
            while (((TryBlockMediator) getMediator()).hasExceptionHandler()) {
                ChildExceptionHandler exHandler = ((TryBlockMediator) getMediator()).popHandler();

                if (exHandler.canHandle(customException)) {
                    exHandler.handleException(carbonMessage, parentCallback);

                    if (mediator.hasNext()) {
                        // If Mediator has a sibling after this
                        try {
                            mediator.next(carbonMessage, parentCallback);
                        } catch (Exception e) {
                            log.error("Error while mediating from Callback", e);
                        }
                    } else if (parentCallback instanceof FlowControllerMediateCallback) {
                        //If no siblings handover message to the requester
                        parentCallback.done(carbonMessage);
                    }

                    return;
                }
            }

            if (parentCallback instanceof FlowControllerExceptionCallback) {
                parentCallback.done(carbonMessage);
            } else {
                // If no child handler, use the default exception handler
                this.defaultExceptionHandler.handleException(carbonMessage, parentCallback);
            }
        } else {
            if (parentCallback instanceof FlowControllerCallback) {
                parentCallback.done(carbonMessage);
            } else {
                new DefaultExceptionHandler().handleException(carbonMessage, parentCallback);
            }
        }
    }

    public Mediator getMediator() {
        return mediator;
    }

    @Override
    public boolean canProcess(CarbonMessage carbonMessage) {
        return carbonMessage.isFaulty();
    }
}
