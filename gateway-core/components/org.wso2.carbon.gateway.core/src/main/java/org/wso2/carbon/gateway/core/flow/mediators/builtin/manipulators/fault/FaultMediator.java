/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.gateway.core.flow.mediators.builtin.manipulators.fault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.AbstractMediator;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * A Mediator used to define custom error messages
 */
public class FaultMediator extends AbstractMediator {


    private static final Logger logger = LoggerFactory.getLogger(FaultMediator.class);

    private String code;
    private String reason;
    private String soapVersion;

    private Map<Class, ErrorDetail> errorDetailMap = new HashMap<>();
    private Map<String, ErrorDetail> stringErrorDetailMap = new HashMap<>();


    @Override
    public String getName() {
        return "fault";
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        Throwable throwable = (Throwable) carbonMessage.getProperty(Constants.ERROR_EXCEPTION);
       String className = throwable.getClass().getName();
        ErrorDetail errorDetail = stringErrorDetailMap.get(className);
        if(errorDetail == null){
            for (Map.Entry entry: errorDetailMap.entrySet()){
              Class<? extends Throwable> obj =  (Class)entry.getKey();
//                if(throwable instanceof ){
//
//                }
            }
        }


        return false;
    }


    public void addErrorCondition(String exceptionClass, ErrorDetail errorDetail) {
        try {
            stringErrorDetailMap.put(exceptionClass, errorDetail);
            Class cl = FaultMediator.class.getClassLoader().loadClass(exceptionClass);
            errorDetailMap.put(cl, errorDetail);
        } catch (ClassNotFoundException e) {
            logger.error("Cannot load exception class " + exceptionClass, e);
        }


    }

}
