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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.Constants;
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
 * This class contains the mediation logic for the Data Mapper Mediator
 */
public class DataMapperMediator extends AbstractMediator {

    private static final Logger log = LoggerFactory.getLogger(DataMapperMediator.class);
    private static final String DM_CONF_DIR = "deployment" + File.separator + "resources" + File.separator + "datamapper";
    private String configKey;
    private String inSchemaKey;
    private String outSchemaKey;
    private MappingResource mappingResource = null;
    private String inputType;
    private String outputType;

    public DataMapperMediator() {
    }

    @Override
    public void setParameters(ParameterHolder parameterHolder) {
        try {
            //PENDING: extracting parameters will change after the custom-mediator implementation is complete.
            Map<String, String> parameters = new HashMap<>();
            String paramString = parameterHolder.getParameter("parameters").getValue();
            String[] paramArray = paramString.split(",");

            for (String param : paramArray) {
                String[] params = param.split("=", 2);
                if (params.length == 2) {
                    parameters.put(params[0].trim(), params[1].trim());
                }
            }
            
            if (parameters.containsKey(Constants.INPUT_TYPE)) {
                inputType = parameters.get(Constants.INPUT_TYPE);
            } else {
                handleException("DataMapper mediator : InputType is not specified");
            }

            if (parameters.containsKey(Constants.OUTPUT_TYPE)) {
                outputType = parameters.get(Constants.OUTPUT_TYPE);
            } else {
                handleException("DataMapper mediator : OutputType is not specified");
            }

            if (parameters.containsKey(Constants.CONFIG)) {
                configKey = parameters.get(Constants.CONFIG);
            } else {
                handleException("DataMapper mediator : mapping configuration is not specified");
            }
            
            if (parameters.containsKey(Constants.INPUT_SCHEMA)) {
                inSchemaKey = parameters.get(Constants.INPUT_SCHEMA);
            } else {
                handleException("DataMapper mediator : input schema is not specified");
            }

            if (parameters.containsKey(Constants.OUTPUT_SCHEMA)) {
                outSchemaKey = parameters.get(Constants.OUTPUT_SCHEMA);
            } else {
                handleException("DataMapper mediator : output schema is not specified");
            }

            mappingResource = getMappingResource();
        } catch (Exception ex) {
            log.error("DataMapper Mediator : Error while setting parameters", ex);
            //TODO: Do proper error handling here. Currently there is no way to fail the deployment for missing configs
        }
    }

    @Override
    public String getName() {
        return "datamap";
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
            //TODO: read this from a config
            String dmExecutorPoolSize = "100";

            MappingHandler mappingHandler = new MappingHandler(mappingResource, inputType, outputType,
                    dmExecutorPoolSize);

            //execute mapping on the input stream
            String outputResult = mappingHandler.doMap(getPayloadStream(cMsg));

            DefaultCarbonMessage transformedCarbonMsg = new DefaultCarbonMessage();
            transformedCarbonMsg.setStringMessageBody(outputResult);

            // Copy Properties
            for (String key : cMsg.getProperties().keySet()) {
                transformedCarbonMsg.setProperty(key, cMsg.getProperty(key));
            }

            // Set message headers
            transformedCarbonMsg.setHeader(Constants.HTTP_CONTENT_TYPE, getContentType(outputType));
            transformedCarbonMsg.setHeader(Constants.HTTP_CONTENT_LENGTH,
                    String.valueOf(outputResult.getBytes(Charset.forName(StandardCharsets.UTF_8.toString())).length));

            return transformedCarbonMsg;
        } catch (ReaderException | InterruptedException | SchemaException | IOException | WriterException e) {
            handleException("DataMapper mediator : mapping failed", e);
        }

        return null; // we never get here, just to make compiler happy
    }
    
    /**
     * Get the content-type for a given output-type
     * 
     * @param outputType
     * @return
     */
    private String getContentType(String outputType) {
        if (Constants.JSON.equalsIgnoreCase(outputType)) {
            return Constants.MEDIA_TYPE_APPLICATION_JSON;
        } else if (Constants.XML.equalsIgnoreCase(outputType)) {
            return Constants.MEDIA_TYPE_APPLICATION_XML;
        } else {
            return outputType;
        }
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
            return new MappingResource(inputSchemaStream, outputSchemaStream, configFileInputStream, outputType);
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
        BlockingQueue<ByteBuffer> msgBody = new LinkedBlockingQueue<>(msg.getCopyOfFullMessageBody());
        return new ByteBufferBackedInputStream(msgBody);
    }

}

