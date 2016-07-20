package org.wso2.carbon.gateway.core.config.annotations;

/**
 * IAnnotation for simple String value annotations.
 */
public class StringAnnotation extends Annotation<String> {

    public StringAnnotation(String name) {
        super(name);
    }

    public StringAnnotation(String name, String value) {
        super(name, value);
    }

}
