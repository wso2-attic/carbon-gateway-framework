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
package org.wso2.carbon.gateway.mediators.datamapper.engine.output.writers;

import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.WriterException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.schemas.Schema;
import org.wso2.carbon.gateway.mediators.datamapper.engine.utils.InputOutputDataType;

/**
 * This class is a factory class to get {@link Writer} needed by the data mapper engine
 */
public class WriterFactory {

    public static Writer getWriter(InputOutputDataType outputType, Schema outputSchema)
            throws SchemaException, WriterException {
        switch (outputType) {
            case XML:
                return new XMLWriter(outputSchema);
            case JSON:
                return new JSONWriter(outputSchema);
            case CSV:
                return new CSVWriter(outputSchema);
            default:
                throw new IllegalArgumentException("Output Writer for type " + outputType + " is not implemented.");
        }
    }
}
