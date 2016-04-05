/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.wso2.carbon.gateway.converters.jsontoxml;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.xml.util.PrettyXMLEventWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.AbstractTypeConverter;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class JSONtoXMLConverter extends AbstractTypeConverter {
    private static final Logger log = LoggerFactory.getLogger(JSONtoXMLConverter.class);

    public InputStream convert(InputStream input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).build();

        try {
            XMLEventReader reader = new JsonXMLInputFactory(config).createXMLEventReader(input);

            XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(output);
            writer = new PrettyXMLEventWriter(writer);

            writer.add(reader);

            reader.close();
            writer.close();

            output.close();
            input.close();
        }
        catch (XMLStreamException e) {
            log.error("Error in parsing the XML Stream", e);
        }
        catch (IOException e) {
            log.error("Error in I/O", e);
        }

        byte[] xml = output.toByteArray();
        return new ByteArrayInputStream(xml);
    }

    public String toString() {
        return getClass().getCanonicalName() + ": " + "converts JSON data to XML data";
    }
}