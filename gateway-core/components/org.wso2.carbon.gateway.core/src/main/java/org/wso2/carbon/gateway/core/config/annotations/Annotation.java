package org.wso2.carbon.gateway.core.config.annotations;

/**
 * Abstract generic bsae for IAnnotation implementations.
 */
public abstract class Annotation<T> implements IAnnotation<T> {

    private String name;
    private T value;

    public Annotation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
