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
package org.wso2.carbon.gateway.mediators.datamapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.config.Parameter;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.flow.AbstractMediator;
import org.wso2.carbon.gateway.core.flow.contentaware.ByteBufferBackedInputStream;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.ReaderException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.WriterException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.mapper.MappingHandler;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.mapper.MappingResource;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

/**
 * Sample Custom Mediator
 */
public class DataMapperMediator extends AbstractMediator {

    private static final Logger log = LoggerFactory.getLogger(DataMapperMediator.class);

    private static final String DM_CONF_DIR = "deployment" + File.separator + "resources" + File.separator + "datamapper";

    private String configKey, inSchemaKey, outSchemaKey;
    private MappingResource mappingResource = null;
    private String inputType, outputType;

    public DataMapperMediator() {
    }

    public void setParameters(ParameterHolder parameterHolder) {
        try {
            Parameter inputTypeParam = parameterHolder.getParameter("input-type");
            if (inputTypeParam == null) {
                handleException("DataMapper mediator : InputType is not specified");
            } else { // else is not required, but to make findbugs happy
                inputType = inputTypeParam.getValue();
            }

            Parameter outputTypeParam = parameterHolder.getParameter("output-type");
            if (outputTypeParam == null) {
                handleException("DataMapper mediator : OutputType is not specified");
            } else {
                outputType = outputTypeParam.getValue();
            }

            Parameter configParam = parameterHolder.getParameter("config");
            if (configParam == null) {
                handleException("DataMapper mediator : mapping configuration is not specified");
            } else {
                configKey = configParam.getValue();
            }

            Parameter inSchemaParam = parameterHolder.getParameter("input-schema");
            if (inSchemaParam == null) {
                handleException("DataMapper mediator : input schema is not specified");
            } else {
                inSchemaKey = inSchemaParam.getValue();
            }

            Parameter outSchemaParam = parameterHolder.getParameter("output-schema");
            if (outSchemaParam == null) {
                handleException("DataMapper mediator : output schema is not specified");
            } else {
                outSchemaKey = outSchemaParam.getValue();
            }

            mappingResource = getMappingResource();
        } catch (Exception ex) {
            log.error("DataMapper Mediator : Error while setting parameters", ex);
            //TODO: Do proper error handling here. Currently there is no way to fail the deployment for missing configs
            //TODO: parameters
        }
    }

    @Override
    public String getName() {
        return "SampleCustomMediator";
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        CarbonMessage transformedCarbonMsg = transform(carbonMessage);
        return next(transformedCarbonMsg, carbonCallback);
    }

    /**
     * Does message conversion and gives the output message as the final result
     */
    private CarbonMessage transform(CarbonMessage cMsg) throws Exception {
        try {

            String dmExecutorPoolSize = "100";

            MappingHandler mappingHandler = new MappingHandler(mappingResource, inputType, outputType,
                                                               dmExecutorPoolSize);

            //execute mapping on the input stream
            String outputResult = mappingHandler.doMap(getPayloadStream(cMsg));

            // Due to a bug we have to create a new carbon message
            DefaultCarbonMessage transformedCarbonMsg = new DefaultCarbonMessage();
            transformedCarbonMsg.setStringMessageBody(outputResult);

            // Copy Properties
            for (String key : cMsg.getProperties().keySet()) {
                transformedCarbonMsg.setProperty(key, cMsg.getProperty(key));
            }

            //TODO: Need to improve this logic, we shouldn't hard code the content-type here
            if (Constants.JSON.equalsIgnoreCase(outputType)) {
                transformedCarbonMsg.setHeader(Constants.HTTP_CONTENT_TYPE, Constants.MEDIA_TYPE_APPLICATION_JSON);
            } else if (Constants.XML.equalsIgnoreCase(outputType)) {
                transformedCarbonMsg.setHeader(Constants.HTTP_CONTENT_TYPE, Constants.MEDIA_TYPE_APPLICATION_XML);
            } else {
                transformedCarbonMsg.setHeader(Constants.HTTP_CONTENT_TYPE, outputType);
            }
            transformedCarbonMsg.setHeader(Constants.HTTP_CONTENT_LENGTH,
                    (String.valueOf(outputResult.getBytes(Charset.forName(StandardCharsets.UTF_8.toString())).length)));

            //ByteBuffer outputByteBuffer = ByteBuffer.wrap(outputResult.getBytes(Charset.forName("UTF-8")));
            //cMsg.addMessageBody(outputByteBuffer);
            //cMsg.setEndOfMsgAdded(true);

            return transformedCarbonMsg;

        } catch (ReaderException | InterruptedException | SchemaException
                | IOException | WriterException e) {
            handleException("DataMapper mediator : mapping failed");
        }

        return null; // we never get here, just to make compiler happy
    }

    /**
     * When Data mapper mediator has been invoked initially, this creates a new mapping resource
     * loader
     *
     * @throws Exception
     */
    private MappingResource getMappingResource() throws Exception {
        InputStream configFileInputStream = getConfigStream(configKey);
        InputStream inputSchemaStream = getConfigStream(inSchemaKey);
        InputStream outputSchemaStream = getConfigStream(outSchemaKey);
        try {
            // Creates a new mappingResourceLoader
            return new MappingResource(inputSchemaStream, outputSchemaStream, configFileInputStream);
        } catch (SchemaException | JSException e) {
            handleException(e.getMessage());
        }
        return null;
    }

    private InputStream getConfigStream(String filename) throws Exception {
        File file = new File(DM_CONF_DIR + File.separator + filename);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            handleException("Datamapper Resource file : " + filename + "not found");
        }
        return inputStream;
    }

    private InputStream getPayloadStream(CarbonMessage msg) {
        BlockingQueue<ByteBuffer> msgBody = new LinkedBlockingQueue<>(msg.getFullMessageBody());
        return new ByteBufferBackedInputStream(msgBody);
    }

}
