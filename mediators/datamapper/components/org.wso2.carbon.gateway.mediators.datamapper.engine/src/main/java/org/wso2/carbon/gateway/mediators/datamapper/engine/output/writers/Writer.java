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

import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.WriterException;

/**
 * This interface should be implemented by data-mapper output writers.
 */
public interface Writer {
    
    /**
     * Write the start of an object in the message.
     * 
     * @param name  Object Name
     * @throws      WriterException
     */
    void writeStartObject(String name) throws WriterException;

    /**
     * Write a field in the message.
     * 
     * @param name  Name of the field
     * @param value Value of the field
     * @throws      WriterException
     */
    void writeField(String name, Object value) throws WriterException;

    /**
     * Write the end of an object in the message.
     * 
     * @param objectName    Name of the object
     * @throws              WriterException
     */
    void writeEndObject(String objectName) throws WriterException;

    /**
     * Terminate message building.
     * 
     * @return  Built message
     * @throws  WriterException
     */
    String terminateMessageBuilding() throws WriterException;

    /**
     * Write start of an array in the message.
     * 
     * @throws WriterException
     */
    void writeStartArray() throws WriterException;

    /**
     * Write end of an array in the message.
     * 
     * @throws WriterException
     */
    void writeEndArray() throws WriterException;

    /**
     * Write start of an annonymous object in the message.
     * 
     * @throws WriterException
     */
    void writeStartAnonymousObject() throws WriterException;

    /**
     * Write a primitive value in the message.
     * 
     * @param value     Premitive value 
     * @throws          WriterException
     */
    void writePrimitive(Object value) throws WriterException;

}
