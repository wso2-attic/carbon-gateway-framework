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
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of a type converter registry
 */
public class BaseTypeConverterRegistry implements TypeConverterRegistry {

    private static final Logger log = LoggerFactory.getLogger(BaseTypeConverterRegistry.class);
    private static BaseTypeConverterRegistry baseTypeConverterRegistry;

    protected final Map<TypeMapper, TypeConverter> typeMapping = new HashMap<>();

    private BaseTypeConverterRegistry() {
        File convertersFile;
        try {
            String carbonHome = System.getProperty("carbon.home");
            convertersFile = new File(carbonHome + File.separator + "conf" + File.separator + "content-aware-mediation"
                                        + File.separator + "type-converters.yml");

            InputStream inputStream = new FileInputStream(convertersFile);
            Yaml yaml = new Yaml();

            Map<String, Object> rootMap = (Map<String, Object>) yaml.load(inputStream);
            List<Map<String, String>> convertersList = (List<Map<String, String>>)
                                                            rootMap.get("converterConfigurations");

            convertersList.forEach(converterEntry -> {
                File file = new File(System.getProperty("carbon.home") + File.separator + "deployment"
                            + File.separator + "type-converters" + File.separator + converterEntry.get("artifactId"));
                URL url;

                ClassLoader loader;
                Class clazz;

                try {
                    url = file.toURI().toURL();
                    loader = URLClassLoader.newInstance(new URL[]{url}, getClass().getClassLoader());
                    clazz = loader.loadClass(converterEntry.get("converterClass"));
                    TypeMapper mapper = new TypeMapper(converterEntry.get("to"), converterEntry.get("from"));
                    typeMapping.put(mapper, (TypeConverter) clazz.newInstance());
                    log.info(typeMapping.toString());
                } catch (MalformedURLException e) {
                    log.error("URL of the artifact not valid", e);
                } catch (ClassNotFoundException e) {
                    log.error("Specified converter not found: " + converterEntry.get("converterClass"));
                } catch (InstantiationException e) {
                    log.error("Specified class cannot be instantiated: " + converterEntry.get("converterClass"));
                } catch (IllegalAccessException e) {
                    log.error("Specified class cannot be accessed: " + converterEntry.get("converterClass"));
                }
            });

            inputStream.close();
        } catch (IOException e) {
            log.error("File not found", e);
        }
    }

    public static synchronized BaseTypeConverterRegistry getInstance() {
        if (baseTypeConverterRegistry == null) {
            baseTypeConverterRegistry = new BaseTypeConverterRegistry();
        }
        return baseTypeConverterRegistry;
    }

    @Override
    public void addTypeConverter(Class<?> toType, Class<?> fromType, TypeConverter typeConverter) {
        log.trace("Adding type converter: {}", typeConverter);
        TypeMapper key = new TypeMapper(toType.getName(), fromType.getName());
        TypeConverter converter = typeMapping.get(key);
        if (typeConverter != converter) {
            if (converter == null) {
                typeMapping.put(key, typeConverter);
            }
        }
    }

    @Override
    public void addTypeConverter(String targetType, String sourceType, TypeConverter typeConverter) {
        log.trace("Adding type converter: {}", typeConverter);
        TypeMapper key = new TypeMapper(targetType, sourceType);
        TypeConverter converter = typeMapping.get(key);
        if (typeConverter != converter) {
            if (converter == null) {
                typeMapping.put(key, typeConverter);
            }
        }
    }

    @Override
    public boolean removeTypeConverter(Class<?> toType, Class<?> fromType) {
        log.trace("Removing type converter from: {} to: {}", fromType, toType);
        TypeMapper key = new TypeMapper(toType.getName(), fromType.getName());
        TypeConverter converter = typeMapping.remove(key);
        if (converter != null) {
            typeMapping.remove(key);
        }
        return converter != null;
    }

    public TypeConverter getTypeConverter(Class<?> targetType, Class<?> sourceType) {
        TypeMapper key = new TypeMapper(targetType.getName(), sourceType.getName());
        return typeMapping.get(key);
    }

    public TypeConverter getTypeConverter(String targetType, String sourceType) {
        TypeMapper key = new TypeMapper(targetType, sourceType);
        return typeMapping.get(key);
    }

    @Override
    public TypeConverter lookup(String targetType, String sourceType) {
        return typeMapping.get(new TypeMapper(targetType, sourceType));
    }

}

