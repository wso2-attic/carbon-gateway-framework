package org.wso2.carbon.gateway.core.flow;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Wrapper class for holding carbon message and carbon callback along with MEP ID.
 */
public class RxContext {

    private String id;
    private String name;
    private CarbonMessage cMsg;
    private CarbonCallback cCallback;

    public RxContext(String id, CarbonMessage cMsg, CarbonCallback cCallback) {
        this.id = id;
        this.cMsg = cMsg;
        this.cCallback = cCallback;
        this.name = null;
    }

    public RxContext(String id, CarbonMessage cMsg, String contextName) {
        this.id = id;
        this.name = contextName;
        this.cMsg = cMsg;
        this.cCallback = null;
    }

    public String getId() {
        return id;
    }

    public CarbonMessage getCarbonMessage() {
        return cMsg;
    }

    public CarbonCallback getCarbonCallback() {
        return cCallback;
    }

    public String getName() {
        return name;
    }
}
