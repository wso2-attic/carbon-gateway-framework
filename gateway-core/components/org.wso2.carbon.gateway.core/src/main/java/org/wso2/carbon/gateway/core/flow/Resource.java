package org.wso2.carbon.gateway.core.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.config.AnnotationNotSupportedException;
import org.wso2.carbon.gateway.core.config.ConfigConstants;
import org.wso2.carbon.gateway.core.config.annotations.Annotation;
import org.wso2.carbon.gateway.core.config.annotations.IAnnotation;
import org.wso2.carbon.gateway.core.config.annotations.common.Description;
import org.wso2.carbon.gateway.core.config.annotations.integration.Path;
import org.wso2.carbon.gateway.core.config.annotations.resource.http.methods.Delete;
import org.wso2.carbon.gateway.core.config.annotations.resource.http.methods.Get;
import org.wso2.carbon.gateway.core.config.annotations.resource.http.methods.Post;
import org.wso2.carbon.gateway.core.config.annotations.resource.http.methods.Put;
import org.wso2.carbon.gateway.core.flow.templates.uri.URITemplate;
import org.wso2.carbon.gateway.core.util.VariableUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import rx.Observable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Object model representing a single resource.
 */
public class Resource {
    // TODO: Does this need an interface
    private static final Logger log = LoggerFactory.getLogger(Resource.class);

    /**
     * Metadata holders
     */
    private String name;
    private Map<String, Annotation> annotations = new HashMap<>();
    private Worker defaultWorker;
    private URITemplate path;

    public Resource(String name) {
        this.name = name;
        defaultWorker = new Worker(name);
        /* Predefined annotations */
        annotations.put(ConfigConstants.AN_DESCRIPTION, new Description());
        annotations.put(ConfigConstants.GET_ANNOTATION, new Get());
        annotations.put(ConfigConstants.PUT_ANNOTATION, new Put());
        annotations.put(ConfigConstants.POST_ANNOTATION, new Post());
        annotations.put(ConfigConstants.DELETE_ANNOTATION, new Delete());
        annotations.put(ConfigConstants.AN_BASE_PATH, new Path());
    }

    public Resource(String name, URITemplate uriTemplate) {
        this.name = name;
        this.path = uriTemplate;
        defaultWorker = new Worker(name);
        /* Predefined annotations */
        annotations.put(ConfigConstants.AN_DESCRIPTION, new Description());
        annotations.put(ConfigConstants.GET_ANNOTATION, new Get());
        annotations.put(ConfigConstants.PUT_ANNOTATION, new Put());
        annotations.put(ConfigConstants.POST_ANNOTATION, new Post());
        annotations.put(ConfigConstants.DELETE_ANNOTATION, new Delete());
        annotations.put(ConfigConstants.AN_BASE_PATH, new Path());

    }

    public void setUritemplate (URITemplate path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        log.info("Resource " + name + " received a message.");

        Map<String, Observable> observableMap = new LinkedHashMap<>();
        carbonMessage.setProperty("OBSERVABLES", observableMap);

        defaultWorker.submit(UUID.randomUUID(), carbonMessage, carbonCallback).subscribe(r -> log.info(
                "Resource subscribe event " + ((RxContext) r).getId())); // we don't need subscriber to return here?

        return true;
    }

    public Annotation getAnnotation(String name) {
        return annotations.get(name);
    }

    public Map<String, Annotation> getAnnotations() {
        return annotations;
    }

    public void addAnnotation(String name, IAnnotation value) throws AnnotationNotSupportedException {
        if (annotations.get(name) != null) {
            annotations.get(name).setValue(value);
        } else {
            throw new AnnotationNotSupportedException("Annotation " + name + " is not supported by Integration");
        }
    }

    public Worker getDefaultWorker() {
        return defaultWorker;
    }

    public boolean matches(CarbonMessage cMsg) {
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
        uriVars.forEach((k, v) -> VariableUtil
                .addGlobalVariable(cMsg, k, VariableUtil.createVariable(Constants.TYPES.STRING, v)));
    }
}
