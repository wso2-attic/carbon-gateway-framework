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

package org.wso2.carbon.gateway.converters.xmltojson;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.AbstractTypeConverter;
import org.wso2.carbon.gateway.core.flow.contentaware.exceptions.TypeConversionException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;

/**
 * This converts XML to JSON input stream
 */
public class XMLtoJSONTypeConverter extends AbstractTypeConverter {

    @Override
    public InputStream convert(InputStream inputStream) throws TypeConversionException, IOException {
        Logger log = LoggerFactory.getLogger(XMLtoJSONTypeConverter.class);
        InputStream input = inputStream;
        InputStream results = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        JsonXMLConfig config = new JsonXMLConfigBuilder().autoArray(true).autoPrimitive(true).prettyPrint(true).build();
        try {
            //Create source (XML).
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(input);
            Source source = new StAXSource(reader);

            //Create result (JSON).
            XMLStreamWriter writer = new JsonXMLOutputFactory(config).createXMLStreamWriter(output);
            Result result = new StAXResult(writer);

            //Copy source to result via "identity transform".
            TransformerFactory.newInstance().newTransformer().transform(source, result);

            byte[] outputByteArray = output.toByteArray();
            results = new ByteArrayInputStream(outputByteArray);

        } catch (TransformerConfigurationException e) {
            log.error("Error in parsing the JSON Stream", e);
        } catch (TransformerException e) {
            log.error("Error in parsing the JSON Stream", e);
        } catch (XMLStreamException e) {
            log.error("Error in parsing the XML Stream", e);
        } finally {
            //As per StAX specification, XMLStreamReader/Writer.close() doesn't close the underlying stream.
            output.close();
            input.close();
        }

        return results;
    }
}
