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

package org.wso2.carbon.gateway.core.flow.contentaware.messagereaders;

import org.wso2.carbon.gateway.core.flow.contentaware.MIMEType;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A manager class for select builder and manage builders
 */
public class ReaderProviderRegistry implements ReaderProvider {

    private static final ReaderProviderRegistry builder = new ReaderProviderRegistry();

    private Map<String, Reader> builderMap = new ConcurrentHashMap<>();

    private ReaderProviderRegistry() {
        builderMap.put(MIMEType.APPLICATION_SOAP_XML, new SOAPReader(MIMEType.APPLICATION_SOAP_XML));
        builderMap.put(MIMEType.TEXT_XML, new SOAPReader(MIMEType.TEXT_XML));
        builderMap.put(MIMEType.APPLICATION_XML, new ApplicationXMLReader(MIMEType.APPLICATION_XML));
        builderMap.put(MIMEType.APPLICATION_JSON, new JSONReader(MIMEType.APPLICATION_JSON));
    }

    public Reader getReader(CarbonMessage carbonMessage) {
        String contentType = carbonMessage.getHeader(Constants.HTTP_CONTENT_TYPE);
        if (builderMap.containsKey(contentType)) {
            return builderMap.get(contentType);
        } else {
            return builderMap.get(MIMEType.TEXT_XML);
        }
    }

    public void registerBuilder(String contentType, Reader reader) {
        builderMap.put(contentType, reader);

    }

    public void unregisterBuilder(String contentType, Reader reader) {
        builderMap.remove(contentType, reader);

    }

    public static ReaderProviderRegistry getInstance() {
        return builder;
    }
}
