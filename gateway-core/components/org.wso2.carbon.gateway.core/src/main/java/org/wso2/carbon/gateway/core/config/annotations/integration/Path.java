package org.wso2.carbon.gateway.core.config.annotations.integration;


import org.wso2.carbon.gateway.core.config.ConfigConstants;
import org.wso2.carbon.gateway.core.config.annotations.StringAnnotation;

public class Path extends StringAnnotation {
    public Path() {
        super(ConfigConstants.AN_BASE_PATH);
    }

    public Path(String value) {
        super(ConfigConstants.AN_BASE_PATH, value);
    }
}
