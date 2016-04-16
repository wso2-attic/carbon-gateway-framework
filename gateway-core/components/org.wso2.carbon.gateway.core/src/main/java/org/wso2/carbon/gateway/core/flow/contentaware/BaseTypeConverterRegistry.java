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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.TypeConverter;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.TypeConverterRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Base implementation of a type converter registry
 */
public class BaseTypeConverterRegistry implements TypeConverterRegistry {

    private static final Logger log = LoggerFactory.getLogger(BaseTypeConverterRegistry.class);

    private static BaseTypeConverterRegistry instance = new BaseTypeConverterRegistry();

    protected final Map<TypeMapper, TypeConverter> typeConverters = new HashMap<>();

    private BaseTypeConverterRegistry() {
    }

    public static BaseTypeConverterRegistry getInstance() {
        return instance;
    }

    @Override
    public void addTypeConverter(Class<?> toType, Class<?> fromType, TypeConverter typeConverter) {
        if (log.isDebugEnabled()) {
            log.debug("Adding type converter: {}", typeConverter);
        }
        typeConverters.put(new TypeMapper(toType.getName(), fromType.getName()), typeConverter);
    }

    @Override
    public void addTypeConverter(String targetType, String sourceType,
                                 TypeConverter typeConverter) {
        if (log.isDebugEnabled()) {
            log.debug("Adding type converter: {}", typeConverter);
        }
        typeConverters.put(new TypeMapper(targetType, sourceType), typeConverter);
    }

    @Override
    public boolean removeTypeConverter(Class<?> toType, Class<?> fromType) {
        if (log.isDebugEnabled()) {
            log.debug("Removing type converter from: {} to: {}", fromType, toType);
        }
        TypeConverter converter =
                typeConverters.remove(new TypeMapper(toType.getName(), fromType.getName()));

        return converter != null;
    }

    @Override
    public boolean removeTypeConverter(String targetType, String sourceType) {

        if (log.isDebugEnabled()) {
            log.debug("Removing type converter from: {} to: {}", sourceType, targetType);
        }
        TypeConverter converter = typeConverters.remove(new TypeMapper(targetType, sourceType));

        return converter != null;
    }

    public TypeConverter getTypeConverter(Class<?> targetType, Class<?> sourceType) {
        TypeMapper key = new TypeMapper(targetType.getName(), sourceType.getName());
        return typeConverters.get(key);
    }

    public TypeConverter getTypeConverter(String targetType, String sourceType) {
        TypeMapper key = new TypeMapper(targetType, sourceType);
        return typeConverters.get(key);
    }

    @Override
    public TypeConverter lookup(String targetType, String sourceType) {
        return typeConverters.get(new TypeMapper(targetType, sourceType));
    }

}

