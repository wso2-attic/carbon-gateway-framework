package org.wso2.carbon.gateway.core.config;


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

    public AnnotationNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
