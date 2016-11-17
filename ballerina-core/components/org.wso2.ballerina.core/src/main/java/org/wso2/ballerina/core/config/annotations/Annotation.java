package org.wso2.ballerina.core.config.annotations;

/**
 * Abstract generic bsae for IAnnotation implementations.
 * @param <T> annotation object type
 */
public abstract class Annotation<T> implements IAnnotation<T> {

    private String name;
    private T value;

    /**
     * Constructor to initialize annotation with a name.
     * @param name
     */
    public Annotation(String name) {
        this.name = name;
    }

    /**
     * Constructor to initialize annotation with a name and value.
     * @param name
     * @param value
     */
    public Annotation(String name, T value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Return the annotation name.
     * @return annotations name.
     */
    public String getName() {
        return name;
    }

    /**
     * Return annotations value object.
     * @return
     */
    public T getValue() {
        return value;
    }

    /**
     * Set annotation value object.
     * @param value object
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Annotations are optional by default.
     * @return true
     */
    @Override
    public boolean isOptional() {
        return true;
    }
}
