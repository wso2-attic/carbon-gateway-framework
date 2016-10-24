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
package org.wso2.carbon.gateway.mediators.datamapper.engine.core.schemas;

import java.util.List;
import java.util.Map;

import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.InvalidPayloadException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.SchemaException;

/**
 * Interface to represent schema in data mapper engine.
 */
public interface Schema {

    /**
     * Method for get defined name of the schema.
     *
     * @return Name of the schema as a String
     */
    String getName() throws SchemaException;

    /**
     * Method to get the element type specified in the schema by giving the element hierarchy.
     *
     * @param elementStack
     * @return type of the element
     */
    String getElementTypeByName(List<SchemaElement> elementStack) throws InvalidPayloadException, SchemaException;

    /**
     * Det the element type specified in the schema by giving the element.
     * 
     * @param elementStack  Element
     * @return              Element type
     * @throws              InvalidPayloadException
     * @throws              SchemaException
     */
    String getElementTypeByName(String elementStack) throws InvalidPayloadException, SchemaException;

    /**
     * Method for check whether schema has a child element inside given element
     *
     * @return
     */
    boolean isChildElement(String elementName, String childElementName);

    
    /**
     * Check whether a element is a child of the given element hierarchy.
     * 
     * @param elementStack      Element hierarchy
     * @param childElementName  Name of the element to be checked
     * @return                  Flag indicating whether the element is a child or not
     * @throws                  InvalidPayloadException
     * @throws                  SchemaException
     */
    boolean isChildElement(List<SchemaElement> elementStack, String childElementName)
            throws InvalidPayloadException, SchemaException;

    /**
     * Get the prefix corresponds to the namespace.
     * 
     * @param url   Namespace
     * @return
     */
    String getPrefixForNamespace(String url);

    /**
     * Get the namespaces as a map.
     * 
     * @return  Map of namespaces
     */
    Map<String, String> getNamespaceMap();

    /**
     * Check whether the array is of primitive data type.
     * 
     * @return
     */
    boolean isCurrentArrayIsPrimitive();

    /**
     * Get the schema as a map.
     * 
     * @return  Schema as a map
     */
    Map getSchemaMap();
}
