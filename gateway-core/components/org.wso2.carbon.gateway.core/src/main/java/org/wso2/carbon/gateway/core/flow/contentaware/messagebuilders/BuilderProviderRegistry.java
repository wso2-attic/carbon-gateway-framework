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

package org.wso2.carbon.gateway.core.flow.contentaware.messagebuilders;

import org.wso2.carbon.gateway.core.flow.contentaware.MIMEType;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A manager class for select builder and manage builders
 */
public class BuilderProviderRegistry implements BuilderProvider {

    private static final BuilderProviderRegistry builder = new BuilderProviderRegistry();

    private Map<String, Builder> builderMap = new ConcurrentHashMap<>();

    private BuilderProviderRegistry() {
        builderMap.put(MIMEType.APPLICATION_SOAP_XML, new SOAPBuilder(MIMEType.APPLICATION_SOAP_XML));
        builderMap.put(MIMEType.TEXT_XML, new SOAPBuilder(MIMEType.TEXT_XML));
        builderMap.put(MIMEType.APPLICATION_XML, new ApplicationXMLBuilder(MIMEType.APPLICATION_XML));
    }

    public Builder getBuilder(CarbonMessage carbonMessage) {
        String contentType = carbonMessage.getHeader(Constants.HTTP_CONTENT_TYPE);
        if (builderMap.containsKey(contentType)) {
            return builderMap.get(contentType);
        } else {
            return builderMap.get(MIMEType.TEXT_XML);
        }
    }

    public void registerBuilder(String contentType, Builder builder) {
        builderMap.put(contentType, builder);

    }

    public void unregisterBuilder(String contentType, Builder builder) {
        builderMap.remove(contentType, builder);

    }

    public static BuilderProviderRegistry getInstance() {
        return builder;
    }
}
