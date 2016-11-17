package org.wso2.ballerina.core.flow;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * This callback is used to control the flow of the message mediation.
 */
public interface FlowControllerCallback extends CarbonCallback {
    boolean canProcess(CarbonMessage carbonMessage);
}
