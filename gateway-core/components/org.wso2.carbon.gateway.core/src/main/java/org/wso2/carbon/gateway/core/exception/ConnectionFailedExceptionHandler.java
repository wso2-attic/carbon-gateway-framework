package org.wso2.carbon.gateway.core.exception;

import org.wso2.carbon.messaging.exceptions.NelException;

/**
 * Can only handle ConnectionTimeoutException.
 */
public class ConnectionFailedExceptionHandler extends ChildExceptionHandler {
    @Override
    public boolean canHandle(NelException nelexception) {
        return ConnectionFailedException.class.isInstance(nelexception);
    }
}
