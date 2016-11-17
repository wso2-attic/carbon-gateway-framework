package org.wso2.ballerina.core.exception;

import org.wso2.carbon.messaging.exceptions.NelException;

/**
 * Can only handle ConnectionTimeoutException.
 */
public class ConnectionTimeoutExceptionHandler extends ChildExceptionHandler {
    @Override
    public boolean canHandle(NelException nelexception) {
        return ConnectionTimeoutException.class.isInstance(nelexception);
    }
}
