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
import org.wso2.carbon.gateway.mediators.datamapper.engine.utils.DataMapperEngineConstants;
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
    
    //TODO: read this from a config
    private int dmExecutorPoolSize = DataMapperEngineConstants.DEFAULT_DATAMAPPER_ENGINE_POOL_SIZE;

    public DataMapperMediator() {
    }

    @Override
    public void setParameters(ParameterHolder parameterHolder) {
        try {
            Parameter inputTypeParam = parameterHolder.getParameter(DataMapperConstants.INPUT_TYPE);
            if (inputTypeParam != null) {
                this.inputType = inputTypeParam.getValue();
            } else {
                handleException("DataMapper mediator: InputType is not specified");
            }

            Parameter outputTypeParam = parameterHolder.getParameter(DataMapperConstants.OUTPUT_TYPE);
            if (outputTypeParam != null) {
                this.outputType = outputTypeParam.getValue();
            } else {
                handleException("DataMapper mediator: OutputType is not specified");
            }

            Parameter configParam = parameterHolder.getParameter(DataMapperConstants.CONFIG);
            if (configParam != null) {
                this.configKey = configParam.getValue();
            } else {
                handleException("DataMapper mediator: Mapping configuration is not specified");
            }
            
            Parameter inSchemaParam = parameterHolder.getParameter(DataMapperConstants.INPUT_SCHEMA);
            if (inSchemaParam != null) {
                this.inSchemaKey = inSchemaParam.getValue();
            } else {
                handleException("DataMapper mediator: Input schema is not specified");
            }

            Parameter outSchemaParam = parameterHolder.getParameter(DataMapperConstants.OUTPUT_SCHEMA);
            if (outSchemaParam != null) {
                this.outSchemaKey = outSchemaParam.getValue();
            } else {
                handleException("DataMapper mediator: Output schema is not specified");
            }

            this.mappingResource = getMappingResource();
        } catch (Exception e) {
            log.error("DataMapper Mediator: Error while setting parameters: " + e.getMessage(), e);
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
            MappingHandler mappingHandler = new MappingHandler(this.mappingResource, this.inputType, this.outputType,
                    this.dmExecutorPoolSize);

            //execute mapping on the input stream
            String outputResult = mappingHandler.doMap(getPayloadStream(cMsg));

            DefaultCarbonMessage transformedCarbonMsg = new DefaultCarbonMessage();
            transformedCarbonMsg.setStringMessageBody(outputResult);

            // Copy Properties
            for (String key : cMsg.getProperties().keySet()) {
                transformedCarbonMsg.setProperty(key, cMsg.getProperty(key));
            }

            // Set message headers
            transformedCarbonMsg.setHeader(Constants.HTTP_CONTENT_TYPE, getContentType(this.outputType));
            transformedCarbonMsg.setHeader(Constants.HTTP_CONTENT_LENGTH,
                    String.valueOf(outputResult.getBytes(StandardCharsets.UTF_8).length));

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
        if (DataMapperConstants.JSON.equalsIgnoreCase(outputType)) {
            return Constants.MEDIA_TYPE_APPLICATION_JSON;
        } else if (DataMapperConstants.XML.equalsIgnoreCase(outputType)) {
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
        InputStream configFileInputStream = getConfigStream(this.configKey);
        InputStream inputSchemaStream = getConfigStream(this.inSchemaKey);
        InputStream outputSchemaStream = getConfigStream(this.outSchemaKey);
        try {
            // Creates a new mappingResourceLoader
            return new MappingResource(inputSchemaStream, outputSchemaStream, configFileInputStream, this.outputType);
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

