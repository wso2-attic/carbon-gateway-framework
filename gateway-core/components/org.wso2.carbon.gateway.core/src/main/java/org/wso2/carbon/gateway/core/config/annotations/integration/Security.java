package org.wso2.carbon.gateway.core.config.annotations.integration;


import org.wso2.carbon.gateway.core.config.ConfigConstants;
import org.wso2.carbon.gateway.core.config.annotations.StringAnnotation;

public class Security extends StringAnnotation {
    public Security() {
        super(ConfigConstants.AN_SECURITY);
    }

    public Security(String value) {
        super(ConfigConstants.AN_SECURITY, value);
    }
}
