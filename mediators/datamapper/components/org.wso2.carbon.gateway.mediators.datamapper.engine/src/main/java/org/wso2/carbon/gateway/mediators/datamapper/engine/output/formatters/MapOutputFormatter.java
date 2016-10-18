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
package org.wso2.carbon.gateway.mediators.datamapper.engine.output.formatters;

import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.WriterException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.models.Model;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.schemas.Schema;
import org.wso2.carbon.gateway.mediators.datamapper.engine.input.readers.events.ReaderEvent;
import org.wso2.carbon.gateway.mediators.datamapper.engine.input.readers.events.ReaderEventType;
import org.wso2.carbon.gateway.mediators.datamapper.engine.output.OutputMessageBuilder;
import org.wso2.carbon.gateway.mediators.datamapper.engine.utils.DataMapperEngineConstants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements {@link Formatter} interface to read {@link Map} model and trigger events
 * to read
 * by {@link OutputMessageBuilder}
 */
public class MapOutputFormatter implements Formatter {

    private static final String XSI_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";
    private OutputMessageBuilder outputMessageBuilder;
    private Schema outputSchema = null;

    @Override
    public void format(Model model, OutputMessageBuilder outputMessageBuilder, Schema outputSchema)
            throws SchemaException, WriterException {
        if (model.getModel() instanceof Map) {
            this.outputMessageBuilder = outputMessageBuilder;
            this.outputSchema = outputSchema;
            Map<String, Object> mapOutputModel = (Map<String, Object>) model.getModel();
            traverseMap(mapOutputModel);
            sendTerminateEvent();
        } else {
            throw new IllegalArgumentException("Illegal model passed to MapOutputFormatter : " + model.getModel());
        }
    }

    /**
     * This method traverse output variable represented as a map in a depth first traverse
     * recursively to trigger events
     * to build output message in {@link OutputMessageBuilder}
     *
     * @param outputMap
     */
    private void traverseMap(Map<String, Object> outputMap) throws SchemaException, WriterException {
        Set<String> mapKeys = outputMap.keySet();
        LinkedList<String> orderedKeyList = new LinkedList<>();
        boolean arrayType = false;
        if (isMapContainArray(mapKeys)) {
            sendArrayStartEvent();
            arrayType = true;
        }
        ArrayList<String> tempKeys = new ArrayList<>();
        tempKeys.addAll(mapKeys);
        //Attributes should come first than other fields. So attribute should be listed first
        for (String key : mapKeys) {
            if (key.contains(DataMapperEngineConstants.SCHEMA_ATTRIBUTE_FIELD_PREFIX) && tempKeys.contains(key)) {
                orderedKeyList.addFirst(key);
                tempKeys.remove(key);
            } else {
                if (key.endsWith(DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX) && tempKeys.contains(key)) {
                    String elementName = key.substring(0, key.lastIndexOf(DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX));
                    orderedKeyList.addLast(key);
                    orderedKeyList.addLast(elementName);
                    tempKeys.remove(key);
                    tempKeys.remove(elementName);
                } else if (tempKeys.contains(key)) {
                    if (tempKeys.contains(key + DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX)) {
                        orderedKeyList.addLast(key + DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX);
                        orderedKeyList.addLast(key);
                        tempKeys.remove(key);
                        tempKeys.remove(key + DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX);
                    } else {
                        orderedKeyList.addLast(key);
                        tempKeys.remove(key);
                    }
                }
            }
        }
        int mapKeyIndex = 0;
        for (String key : orderedKeyList) {
            Object value = outputMap.get(key);
            if (value instanceof Map) {
                // key value is a type of object or an array
                if (arrayType) {
                    /*If it is array type we need to compensate the already created object start
                    element.
                    So avoid create another start element in first array element and endElement
                    in the last
                    */
                    if (mapKeyIndex != 0) {
                        sendAnonymousObjectStartEvent();
                        createAndSendIdentifierFieldEvent(key);
                    }
                    traverseMap((Map<String, Object>) value);
                    if (mapKeyIndex != mapKeys.size() - 1) {
                        sendObjectEndEvent(key);
                    }
                } else {
                    sendObjectStartEvent(key);
                    createAndSendIdentifierFieldEvent(key);
                    traverseMap((Map<String, Object>) value);
                    if (!key.endsWith(DataMapperEngineConstants.SCHEMA_ATTRIBUTE_PARENT_ELEMENT_POSTFIX)) {
                        sendObjectEndEvent(key);
                    }
                }
            } else {
                // Primitive value recieved to write
                if (arrayType) {
                    // if it is an array of primitive values
                    sendPrimitiveEvent(key, value);
                } else {
                    // if field value
                    sendFieldEvent(key, value);
                }
            }
            mapKeyIndex++;
        }
        if (arrayType) {
            sendArrayEndEvent();
        }
    }

    private void createAndSendIdentifierFieldEvent(String key) throws SchemaException, WriterException {
        //sending events to create xsi:type attribute
        Pattern identifierPattern = Pattern.compile("(_.+_type)");
        Matcher matcher = identifierPattern.matcher(key);
        while (matcher.find()) {
            String s = matcher.group(0);
            String stringArray[] = s.split("_");
            String prefix = stringArray[stringArray.length - 2];
            if (prefix.equals(outputSchema.getNamespaceMap().get(XSI_NAMESPACE_URI))) {
                sendFieldEvent("attr_" + prefix + ":type", key.split("_" + prefix + "_type_")[1].replace('_', ':'));
            }
        }
    }

    private void sendPrimitiveEvent(String key, Object value) throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.PRIMITIVE, key, value));
    }

    private void sendAnonymousObjectStartEvent() throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.ANONYMOUS_OBJECT_START, null, null));
    }

    private void sendArrayEndEvent() throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.ARRAY_END, null, null));
    }

    private boolean isMapContainArray(Set<String> mapKeys) {
        for (String key : mapKeys) {
            if (DataMapperEngineConstants.ARRAY_ELEMENT_FIRST_NAME.equals(key)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private void sendArrayStartEvent() throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.ARRAY_START, null, null));
    }

    private void sendObjectStartEvent(String elementName) throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.OBJECT_START, elementName, null));
    }

    private void sendObjectEndEvent(String objectName) throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.OBJECT_END, objectName, null));
    }

    private void sendFieldEvent(String fieldName, Object value) throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.FIELD, fieldName, value));
    }

    private void sendTerminateEvent() throws SchemaException, WriterException {
        getOutputMessageBuilder().notifyEvent(new ReaderEvent(ReaderEventType.TERMINATE, null, null));
    }

    public OutputMessageBuilder getOutputMessageBuilder() {
        return outputMessageBuilder;
    }

}
