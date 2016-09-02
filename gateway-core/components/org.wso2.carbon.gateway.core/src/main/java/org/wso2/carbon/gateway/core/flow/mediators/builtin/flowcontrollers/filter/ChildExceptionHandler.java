package org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.exception.ExceptionHandler;
import org.wso2.carbon.gateway.core.flow.MediatorCollection;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Exception handlers.
 */
public class ChildExceptionHandler implements ExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ChildExceptionHandler.class);

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
