package org.wso2.ballerina.core.exception;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.exceptions.NelException;

/**
 * Exception handler
 */
public interface ExceptionHandler {

    /**
     * This method can handle the exception that is returned by canHandle() method.
     * @param carbonMessage contains the error details.
     * @param callback which notifies what need to be done once error is handled.
     * @return
     */
    public boolean handleException(CarbonMessage carbonMessage, CarbonCallback callback);

    /**
     * This method returns the type of the error that can be handle by the error handler implementation.
     * @return the error type.
     */
    public boolean canHandle(NelException nelException);
}
