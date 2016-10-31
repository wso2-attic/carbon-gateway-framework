package org.wso2.carbon.gateway.core.exception;

import org.wso2.carbon.messaging.exceptions.NelException;

/**
 * Child handlers that can handle a specific exception.
 */
public class CustomExceptionHandler extends ChildExceptionHandler {
    @Override
    public boolean canHandle(NelException nelexception) {
        return CustomException.class.isInstance(nelexception);
    }
}
