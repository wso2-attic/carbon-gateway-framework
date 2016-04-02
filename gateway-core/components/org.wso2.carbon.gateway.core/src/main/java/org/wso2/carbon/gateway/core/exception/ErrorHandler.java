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

package org.wso2.carbon.gateway.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.FlowControllerCallback;
import org.wso2.carbon.gateway.core.flow.MediatorCollection;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.invokers.RespondMediator;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.FaultHandler;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * A FaultHandler class for engine
 */
public class ErrorHandler implements FaultHandler {

    private Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    private MediatorCollection mediatorCollection;

    private CarbonCallback initiatedCallback;

    private String name;


    public ErrorHandler(String name, MediatorCollection mediatorCollection) {
        this.name = name;
        this.mediatorCollection = mediatorCollection;
    }


    @Override
    public void handleFault(String errorcode, Throwable throwable, CarbonMessage carbonMessage,
                            CarbonCallback carbonCallback) {

        initiatedCallback = getSuperParentCallback(carbonCallback);

        if (mediatorCollection.getFirstMediator() instanceof RespondMediator) {
            DefaultCarbonMessage response = new DefaultCarbonMessage();
            String payload = throwable.getMessage();
            response.setStringMessageBody(payload);
            byte[] errorMessageBytes = payload.getBytes(Charset.defaultCharset());

            Map<String, String> transportHeaders = new HashMap<>();
            transportHeaders.put(Constants.HTTP_CONNECTION, Constants.KEEP_ALIVE);
            transportHeaders.put(Constants.HTTP_CONTENT_ENCODING, Constants.GZIP);
            transportHeaders.put(Constants.HTTP_CONTENT_TYPE, Constants.TEXT_PLAIN);
            transportHeaders.put(Constants.HTTP_CONTENT_LENGTH, (String.valueOf(errorMessageBytes.length)));
            transportHeaders.put(Constants.HTTP_STATUS_CODE, errorcode);

            response.setHeaders(transportHeaders);

            initiatedCallback.done(response);

        } else {
            carbonMessage.setProperty(Constants.HTTP_STATUS_CODE, errorcode);
            carbonMessage.setProperty(Constants.ERROR_CODE, errorcode);
            carbonMessage.setProperty(Constants.ERROR_MESSAGE, throwable.getLocalizedMessage());
            carbonMessage.setProperty(Constants.ERROR_DETAIL, throwable.getMessage());
            carbonMessage.setProperty(Constants.ERROR_EXCEPTION, throwable);
            try {
                mediatorCollection.getFirstMediator().receive(carbonMessage, initiatedCallback);
            } catch (Exception e) {
                logger.error("Error occurred inside fault handler " + name, e);
            }
        }
    }

    private CarbonCallback getSuperParentCallback(CarbonCallback carbonCallback) {
        while (carbonCallback instanceof FlowControllerCallback) {
            carbonCallback = ((FlowControllerCallback) carbonCallback).getParentCallback();
        }
        return carbonCallback;
    }
}
