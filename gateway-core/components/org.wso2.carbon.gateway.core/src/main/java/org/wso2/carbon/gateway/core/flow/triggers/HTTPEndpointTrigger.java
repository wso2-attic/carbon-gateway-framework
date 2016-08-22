package org.wso2.carbon.gateway.core.flow.triggers;

import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.flow.templates.uri.URITemplate;
import org.wso2.carbon.gateway.core.inbound.InboundEndpoint;
import org.wso2.carbon.gateway.core.util.VariableUtil;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * EndpointTrigger type related to HTTP Inbound sources.
 */
public class HTTPEndpointTrigger extends EndpointTrigger {

    private static final String name = "HTTPEndpointTrigger";
    private InboundEndpoint source;
    private String condition;
    private URITemplate path;

    public HTTPEndpointTrigger(InboundEndpoint source, String condition, URITemplate path) {
        super(name);
        this.source = source;
        this.condition = condition;
        this.path = path;
    }

    @Override
    public boolean matches(CarbonMessage cMsg) {
        String method = (String) cMsg.getProperty(Constants.SERVICE_METHOD);

        if (condition != null && !method.matches(condition)) { //method is optional
            return false;
        }

        if (!isTemplateMatching(cMsg)) {
            return false;
        }

        return true;
    }

    private boolean isTemplateMatching(CarbonMessage cMsg) {
        String subGroupPath = (String) cMsg.getProperty(Constants.SERVICE_SUB_GROUP_PATH);
        Map<String, String> uriVars = new HashMap<>();
        boolean r = path.matches(subGroupPath, uriVars);

        if (r) {
            addVariables(cMsg, uriVars);
        }

        return r;
    }

    private void addVariables(CarbonMessage cMsg, Map<String, String> uriVars) {
        uriVars.forEach((k, v) ->
                VariableUtil.addGlobalVariable(cMsg, k, VariableUtil.createVariable(Constants.TYPES.STRING, v)));
    }

    public InboundEndpoint getSource() {
        return source;
    }

    public void setSource(InboundEndpoint source) {
        this.source = source;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public URITemplate getPath() {
        return path;
    }

    public void setPath(URITemplate path) {
        this.path = path;
    }

}
