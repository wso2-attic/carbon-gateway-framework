package org.wso2.carbon.gateway.mediators.datamapper.engine.input.readers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.JSException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.ReaderException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.exceptions.SchemaException;
import org.wso2.carbon.gateway.mediators.datamapper.engine.core.schemas.Schema;
import org.wso2.carbon.gateway.mediators.datamapper.engine.input.InputBuilder;
import org.wso2.carbon.gateway.mediators.datamapper.engine.input.builders.JSONBuilder;

import static org.wso2.carbon.gateway.mediators.datamapper.engine.utils.DataMapperEngineConstants.BOOLEAN_ELEMENT_TYPE;
import static org.wso2.carbon.gateway.mediators.datamapper.engine.utils.DataMapperEngineConstants.INTEGER_ELEMENT_TYPE;
import static org.wso2.carbon.gateway.mediators.datamapper.engine.utils.DataMapperEngineConstants.ITEMS_KEY;
import static org.wso2.carbon.gateway.mediators.datamapper.engine.utils.DataMapperEngineConstants.NUMBER_ELEMENT_TYPE;
import static org.wso2.carbon.gateway.mediators.datamapper.engine.utils.DataMapperEngineConstants.PROPERTIES_KEY;
import static org.wso2.carbon.gateway.mediators.datamapper.engine.utils.DataMapperEngineConstants.TEXT;
import static org.wso2.carbon.gateway.mediators.datamapper.engine.utils.DataMapperEngineConstants.TYPE_KEY;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;;

/**
 * This class is responsible for generating the JSON message for the given CSV message.
 */
public class CSVInputReader implements InputReader {

    private static final Log log = LogFactory.getLog(CSVInputReader.class);
    /* JSON schema for input message */
    private Map jsonSchema;
    /* JSON schema of the input message */
    private Schema inputSchema;
    /* JSON message builder instance */
    private JSONBuilder jsonBuilder;
    /* Reference of the InputBuilder object to send the built JSON message */
    private InputBuilder messageBuilder;

    /**
     * Initialize the builder.
     *
     * @throws IOException
     */
    public CSVInputReader() throws IOException {
        this.jsonBuilder = new JSONBuilder();
    }

    @Override
    public void read(InputStream input, Schema inputSchema, InputBuilder messageBuilder) throws ReaderException {
        this.inputSchema = inputSchema;
        this.jsonSchema = inputSchema.getSchemaMap();
        this.messageBuilder = messageBuilder;
        OMXMLParserWrapper parserWrapper = OMXMLBuilderFactory.createOMBuilder(input);
        OMElement root = parserWrapper.getDocumentElement();
        String csvContent = getCSVContent(root);
        try {
            populateCSVContents(csvContent, jsonSchema);
        } catch (IOException | SchemaException | JSException e) {
            throw new ReaderException("Error while parsing CSV input stream. " + e.getMessage());
        }
    }

    /**
     * Populate CSV content to a JSON message.
     *
     * @param csvContent CSV content extracted from the input message
     * @param jsonSchemaMap respective JSON schema
     * @throws IOException
     * @throws ReaderException
     * @throws SchemaException
     * @throws JSException
     */
    private void populateCSVContents(String csvContent, Map jsonSchemaMap)
            throws IOException, ReaderException, SchemaException, JSException {
        if (csvContent == null) {
            throw new ReaderException(
                    "Request csv data not found. The csv records should contain in a <text></text> tag.");
        }
        String[] lines = csvContent.split("\\r?\\n");

        Map<String, Object> fieldMap;
        List<String> fieldNamesList;

        if (lines.length > 0) {
            /* Retrieve field names from the JSON schema */
            fieldMap = (Map<String, Object>) ((Map<String, Object>) ((ArrayList) jsonSchemaMap.get(ITEMS_KEY)).get(0))
                    .get(PROPERTIES_KEY);
            fieldNamesList = new ArrayList<>(fieldMap.keySet());
            jsonBuilder.writeStartArray();

            for (String line : lines) {
                jsonBuilder.writeStartObject();
                String[] items = line.split(",");
                for (int i = 0; i < items.length; i++) {
                    writeFieldElement(fieldNamesList.get(i), items[i],
                            getElementTypeByName(fieldNamesList.get(i), fieldMap));
                }
                jsonBuilder.writeEndObject();
            }
            jsonBuilder.writeEndArray();
        }
        writeTerminateElement();
    }

    /**
     * Extract CSV content from the input message.
     * 
     * @param element root element
     * @return CSV content as text
     */
    private String getCSVContent(OMElement element) {
        if (element == null) {
            log.warn("No CSV content found in the input file");
        } else if (TEXT.equals(element.getLocalName())) {
            return element.getText();
        } else {
            Iterator<OMElement> it = element.getChildElements();
            if (it.hasNext()) {
                getCSVContent(it.next());
            }
        }
        return null;
    }

    private String getElementTypeByName(String elementName, Map elementMap) {
        return (String) ((Map) elementMap.get(elementName)).get(TYPE_KEY);
    }

    /**
     * Get the input schema.
     * 
     * @return  Input schema
     */
    public Schema getInputSchema() {
        return inputSchema;
    }

    private void writeFieldElement(String fieldName, String valueString, String fieldType)
            throws IOException, JSException, SchemaException, ReaderException {
        switch (fieldType) {
        case BOOLEAN_ELEMENT_TYPE:
            jsonBuilder.writeField(fieldName, Boolean.parseBoolean(valueString), fieldType);
            break;
        case NUMBER_ELEMENT_TYPE:
            jsonBuilder.writeField(fieldName, Double.parseDouble(valueString), fieldType);
            break;
        case INTEGER_ELEMENT_TYPE:
            jsonBuilder.writeField(fieldName, Integer.parseInt(valueString), fieldType);
            break;
        default:
            jsonBuilder.writeField(fieldName, valueString, fieldType);

        }
    }

    private void writeTerminateElement() throws IOException, JSException, SchemaException, ReaderException {
        jsonBuilder.close();
        String jsonBuiltMessage = jsonBuilder.getContent();
        messageBuilder.notifyWithResult(jsonBuiltMessage);
    }
}