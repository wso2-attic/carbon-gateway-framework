package org.wso2.ballerina.core.config.annotations.integration;


import org.wso2.ballerina.core.config.ConfigConstants;
import org.wso2.ballerina.core.config.annotations.StringAnnotation;

/**
 * Integration level annotation @Path used to specify the base path to anchor on. This is optional.
 */
public class Path extends StringAnnotation {
    public Path() {
        super(ConfigConstants.AN_BASE_PATH);
    }

    public Path(String value) {
        super(ConfigConstants.AN_BASE_PATH, value);
    }
}
