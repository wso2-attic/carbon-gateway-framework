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
import org.wso2.carbon.gateway.core.flow.contentaware.MIMEType;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.TypeConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

/**
 * Class for converting a JSON input stream into an XML input stream
 */

public class JSONtoXMLConverter implements TypeConverter {
    private static final Logger log = LoggerFactory.getLogger(JSONtoXMLConverter.class);

    public InputStream convert(InputStream input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).build();

        XMLEventReader reader = null;
        XMLEventWriter writer = null;
        try {
            reader = new JsonXMLInputFactory(config).createXMLEventReader(input);
            writer = XMLOutputFactory.newInstance().createXMLEventWriter(output);
            writer = new PrettyXMLEventWriter(writer);
            writer.add(reader);
        } catch (XMLStreamException e) {
            log.error("Error in parsing the XML Stream", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }

                if (writer != null) {
                    writer.close();
                }
            } catch (XMLStreamException ignore) {
            }
            try {
                output.close();
                input.close();
            } catch (IOException ignore) {
            }
        }

        byte[] xml = output.toByteArray();
        return new ByteArrayInputStream(xml);
    }

    @Override
    public String getSourceType() {
        return MIMEType.JSON;
    }

    @Override
    public String getTargetType() {
        return MIMEType.XML;
    }

    public String toString() {
        return getClass().getCanonicalName() + ": " + "converts JSON data to XML data";
    }
}
