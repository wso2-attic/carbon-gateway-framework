package org.wso2.carbon.gateway.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.MediatorCollection;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Exception handlers.
 */
public class ChildExceptionHandler implements ExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ChildExceptionHandler.class);

    // Contains the sequence of mediators that need to executed
    // in order to handle the exception.
    private MediatorCollection childMediatorList = new MediatorCollection();

    public boolean handleException(CarbonMessage carbonMessage, CarbonCallback callback) {
        try {
            return childMediatorList.getFirstMediator().receive(carbonMessage, callback);
        } catch (Exception e) {
            log.error("Failed to handle exception", e);
            return false;
        }
    }

    public Class canHandle() {
        return CustomException.class;
    }
}
