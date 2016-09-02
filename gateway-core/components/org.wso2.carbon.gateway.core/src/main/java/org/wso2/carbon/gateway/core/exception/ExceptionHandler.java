package org.wso2.carbon.gateway.core.exception;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Exception handler
 */
public interface ExceptionHandler {

    public boolean handleException(CarbonMessage carbonMessage, CarbonCallback callback);

    public Class canHandle();
}
