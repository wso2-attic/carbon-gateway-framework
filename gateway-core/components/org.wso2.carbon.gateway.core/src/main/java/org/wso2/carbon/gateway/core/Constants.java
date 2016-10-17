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

package org.wso2.carbon.gateway.core;

/**
 * Constants for Gateway-core
 */
public class Constants {

    public static final String ERROR_HANDLER = "ERROR_HANDLER";

    public static final String VARIABLE_STACK = "VARIABLE_STACK";

    /**
     * Enumeration of supported types.
     */
    public enum TYPES {
        STRING, INTEGER, BOOLEAN, DOUBLE, FLOAT, LONG, SHORT, XML, JSON, UNKNOWN, MESSAGE
    }

    public static final String GW_GT_SCOPE = "GW_GT_SCOPE";

    public static final String SERVICE_METHOD = "SERVICE_METHOD";
    public static final String SERVICE_CONTEXT = "SERVICE_CONTEXT";
    public static final String SERVICE_SUB_GROUP_PATH = "SERVICE_SUB_GROUP_PATH";

    /*
    * Field CHARACTER_SET_ENCODING
    */
    public static final String CHARACTER_SET_ENCODING = "CHARACTER_SET_ENCODING";

    /**
     * On inbound requests, the detachable input stream can be queried to get
     * the inbound length.  It can also be "detached" from the inbound http stream
     * to allow resources to be freed.
     */
    public static final String DETACHABLE_INPUT_STREAM = "org.apache.axiom.om.util.DetachableInputStream";

    public static final String HTTP_CONTENT_TYPE = "Content-Type";
    public static final String PROTOCOL_VERSION = "PROTOCOL";
    public static final String MEDIA_TYPE_X_WWW_FORM = "application/x-www-form-urlencoded";
    public static final String MEDIA_TYPE_TEXT_XML = "text/xml";
    public static final String MEDIA_TYPE_MULTIPART_RELATED = "multipart/related";
    public static final String MEDIA_TYPE_MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String MEDIA_TYPE_APPLICATION_XML = "application/xml";
    public static final String MEDIA_TYPE_APPLICATION_SOAP_XML = "application/soap+xml";
    public static final String MEDIA_TYPE_APPLICATION_ECHO_XML = "application/echo+xml";
    public static final String MEDIA_TYPE_APPLICATION_JSON = "application/json";
    public static final String MEDIA_TYPE_APPLICATION_JWT = "application/jwt";

    public static final String APPLICATION_XML_BUILDER_ALLOW_DTD = "application.xml.builder.allow.DTD";

    public static final String TRACE_LOGGER = "TRACE_LOGGER";

    public static final String SOAPACTION = "SOAPAction";

    public static final String CHARSET = "charset";

    public static final String HTTP_CONTENT_LENGTH = "Content-Length";

    public static final String HTTP_TRANSFER_ENCODING = "Transfer-Encoding";

    public static final String HTTP_CONNECTION = "Connection";

    public static final String KEEP_ALIVE = "keep-alive";

    public static final String HTTP_SOAP_ACTION = "SOAPAction";

    public static final String HTTP_CONTENT_ENCODING = "Accept-Encoding";

    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";

    public static final String HTTP_STATUS_CODE = "HTTP_STATUS_CODE";

    public static final String TEXT_PLAIN = "text/plain";

    public static final String APPLICATION_XML = "application/xml";

    public static final String GZIP = "gzip";

    public static final String ENDPOINT = "endpoint";

    public static final String ENDPOINT_GRAMMAR_KEYWORD = "EndPoint";

    public static final String EMPTY_STRING = "";


    /* URL sections */
    public static final String PROTOCOL = "protocol";

    public static final String HOST = "host";

    public static final String PORT = "port";

    public static final String CONTEXT = "context";

    /* Mediator parameters  */

    public static final String LOG_MEDIATOR = "log";

    public static final String LEVEL = "level";

    public static final String LEVEL_FULL = "full";

    public static final String LEVEL_CUSTOM = "custom";

    public static final String RETURN_VALUE = "returnVariableKey";

    public static final String INVOKE_STATEMENT = "invoke";

    public static final String CALL_MEDIATOR_NAME = "call";

    public static final String RESPOND_MEDIATOR_NAME = "respond";

    public static final String PROPERTY_MEDIATOR_NAME = "property";

    public static final String INTEGRATION_KEY = "integrationKey";

    public static final String MESSAGE_KEY = "messageKey";

    /* Exception Types */

    public static final String CONN_CLOSED_EX = "ConnectionClosedException";

    public static final String CONN_FAILED_EX = "ConnectionFailedException";

    public static final String CONN_TIMEOUT_EX = "ConnectionTimeoutException";

    public static final String DEFAULT_EX = "Exception";

    /* Variable Handling Constants */

    public static final String VALUE = "value";

    public static final String KEY = "key";

    public static final String TYPE = "type";

    public static final String ASSIGNMENT = "assignment";
}
