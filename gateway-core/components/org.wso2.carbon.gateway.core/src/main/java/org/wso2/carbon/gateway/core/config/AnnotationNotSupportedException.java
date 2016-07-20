package org.wso2.carbon.gateway.core.config;

/**
 * Exception to throw in case an annotation is used where it should not belong.
 */
public class AnnotationNotSupportedException extends Exception {

    public AnnotationNotSupportedException() {
    }

    public AnnotationNotSupportedException(String message) {
        super(message);
    }

    public AnnotationNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnnotationNotSupportedException(Throwable cause) {
        super(cause);
    }

    public AnnotationNotSupportedException(String message, Throwable cause, boolean enableSuppression,
                                           boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
