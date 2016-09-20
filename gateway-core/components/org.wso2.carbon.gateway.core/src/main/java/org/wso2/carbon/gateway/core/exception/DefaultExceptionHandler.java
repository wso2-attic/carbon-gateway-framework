package org.wso2.carbon.gateway.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.exceptions.NelException;

/**
 * Default exception handler.
 */
public class DefaultExceptionHandler implements ExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @Override
    public boolean handleException(CarbonMessage carbonMessage, CarbonCallback callback) {
        log.error("Executing the default fault handler");
        return true;
    }

    @Override
    public boolean canHandle(NelException nelException) {
        return true;
    }
}
