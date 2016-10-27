/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package $package;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.AbstractMediator;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;


/**
* Mediator Implementation
*/
public class ${mediator_name} extends AbstractMediator {

    private static final Logger log = LoggerFactory.getLogger(${mediator_name}.class);
    private String logMessage = "Message received at Sample Mediator";   // Sample Mediator specific variable


    @Override
    public String getName() {
        return "${mediator_name}";
    }

    /**
    * Mediate the message.
    *
    * This is the execution point of the mediator.
    * @param carbonMessage MessageContext to be mediated
    * @param carbonCallback Callback which can be use to call the previous step
    * @return whether mediation is success or not
    **/
    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        log.info("Invoking ${mediator_name} Mediator");
        log.info(logMessage);
        return next(carbonMessage, carbonCallback); //Move forward
    }

    /**
    * Set Parameters
    *
    * @param parameterHolder holder which contains key-value pairs of parameters
    */
    @Override
    public void setParameters(ParameterHolder parameterHolder) {
        // Read paremeters send as key value pairs here.
        // String testParamValue = parameterHolder.getParameter("testValue").getValue();
    }


    /** This is a sample mediator specific method */
    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

}
