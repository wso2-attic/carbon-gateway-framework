package org.wso2.carbon.gateway.core.exception;

import org.wso2.carbon.messaging.exceptions.NelException;

/**
 * Can only handle ConnectionTimeoutException.
 */
public class GeneralExceptionHandler extends ChildExceptionHandler {
    @Override
    public boolean canHandle(NelException nelexception) {
        return true;
    }
}
