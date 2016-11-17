package org.wso2.ballerina.core.config.annotations;

/**
 * IAnnotation interface.
 * @param <T> annotation type object
 */
public interface IAnnotation<T> {

    /**
     * Return the annotation name.
     * @return Annotation name.
     */
    String getName();

    /**
     * Return the annotation value.
     * @return <T> value object
     */
    T getValue();

    /**
     * Set the annotation value.
     * @param value
     */
    void setValue(T value);

    /**
     * Used to identify if annotations are optional.
     * @return boolean
     */
    boolean isOptional();
}
