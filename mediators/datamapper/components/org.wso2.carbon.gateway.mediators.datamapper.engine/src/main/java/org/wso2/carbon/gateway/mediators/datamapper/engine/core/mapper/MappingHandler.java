/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.gateway.mediators.datamapper.engine.core.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.ReaderException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.WriterException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.executors.Executor;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.executors.ScriptExecutorFactory;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.models.Model;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.notifiers.InputVariableNotifier;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.notifiers.OutputVariableNotifier;
import org.wso2.carbon.gateway.mediators.datamapper.engine.input.InputBuilder;
import org.wso2.carbon.gateway.mediators.datamapper.engine.output.OutputMessageBuilder;
import org.wso2.carbon.gateway.mediators.datamapper.engine.utils.InputOutputDataType;
import org.wso2.carbon.gateway.mediators.datamapper.engine.utils.ModelType;

/**
 *  This class contains the methods necessary to transform a message.
 */
public class MappingHandler implements InputVariableNotifier, OutputVariableNotifier {

    private int dmExecutorPoolSize;
    private String inputVariable;
    private String outputVariable;
    private MappingResource mappingResource;
    private OutputMessageBuilder outputMessageBuilder;
    private Executor scriptExecutor;
    private InputBuilder inputBuilder;

    /**
     * Initialize mapping handler.
     * 
     * @param mappingResource       Mapping Resources
     * @param inputType             Input type
     * @param outputType            Output Tpe
     * @param dmExecutorPoolSize    Script executor pool size
     * @throws                      IOException  
     * @throws                      SchemaException
     * @throws                      WriterException
     */
    public MappingHandler(MappingResource mappingResource, String inputType, String outputType,
            int dmExecutorPoolSize) throws IOException, SchemaException, WriterException {

        this.inputBuilder = new InputBuilder(InputOutputDataType.fromString(inputType),
                mappingResource.getInputSchema());

        this.outputMessageBuilder = new OutputMessageBuilder(InputOutputDataType.fromString(outputType),
                ModelType.JAVA_MAP, mappingResource.getOutputSchema());

        this.dmExecutorPoolSize = dmExecutorPoolSize;
        this.mappingResource = mappingResource;
    }

    /**
     * Transform the given message.
     * 
     * @param inputMsg  Message stream to be mapped
     * @return          Transformed message as a string
     * @throws          ReaderException
     * @throws          InterruptedException
     * @throws          IOException
     * @throws          SchemaException
     * @throws          JSException
     */
    public String doMap(InputStream inputMsg)
            throws ReaderException, InterruptedException, IOException, SchemaException, JSException {
        this.scriptExecutor = ScriptExecutorFactory.getScriptExecutor(dmExecutorPoolSize);
        inputBuilder.buildInputModel(inputMsg, this);
        return outputVariable;
    }

    @Override
    public void notifyInputVariable(Object variable) throws SchemaException, JSException, ReaderException {
        this.inputVariable = (String) variable;
        Model outputModel = scriptExecutor.execute(mappingResource, inputVariable);
        try {
            releaseExecutor();
            if (outputModel.getModel() instanceof Map) {
                outputMessageBuilder.buildOutputMessage(outputModel, this);
            } else {
                notifyOutputVariable(outputModel.getModel());
            }

        } catch (InterruptedException | WriterException e) {
            throw new ReaderException(e.getMessage());
        }
    }

    private void releaseExecutor() throws InterruptedException {
        ScriptExecutorFactory.releaseScriptExecutor(scriptExecutor);
        this.scriptExecutor = null;
    }

    @Override
    public void notifyOutputVariable(Object variable) {
        outputVariable = (String) variable;
    }

}
