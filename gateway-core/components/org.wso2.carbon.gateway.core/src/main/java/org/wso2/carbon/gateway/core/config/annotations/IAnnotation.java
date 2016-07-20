package org.wso2.carbon.gateway.core.config.annotations;

/**
 * IAnnotation interface.
 */
public interface IAnnotation<T> {

    String getName();
    T getValue();
    void setValue(T value);

}
