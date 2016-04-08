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

package org.wso2.carbon.gateway.core.flow.contentaware;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.TypeConverter;

/**
 * Service component for type converters
 */
@Component(
        name = "org.wso2.carbon.gateway.core.flow.contentAwareSupport.TypeConverterServiceComponent",
        immediate = true)

public class TypeConverterServiceComponent {
    private final static Logger log = LoggerFactory.getLogger(TypeConverterServiceComponent.class);

    @Reference(
            name = "TypeConverter-Service",
            service = TypeConverter.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeTypeConverter")

    protected void addTypeConverter(TypeConverter typeConverter) {
        log.info(typeConverter.toString());
        BaseTypeConverterRegistry.getInstance()
                .addTypeConverter(typeConverter.getTargetType(), typeConverter.getSourceType(), typeConverter);
    }

    protected void removeTypeConverter(TypeConverter typeConverter) {
        BaseTypeConverterRegistry.getInstance()
                .removeTypeConverter(typeConverter.getTargetType(), typeConverter.getSourceType());
    }

}
