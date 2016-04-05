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

package org.wso2.carbon.gateway.core.flow.contentAwareSupport.abstractContext;

import org.wso2.carbon.gateway.core.flow.contentAwareSupport.exceptions.TypeConverterExistsException;

/**
 * Registry for type converters.
 *
 */
public interface TypeConverterRegistry {

    /**
     * Registers a new type converter.
     * <p/>
     * This method may throw {@link TypeConverterExistsException} if configured to fail if an existing
     * type converter already exists
     *
     * @param toType        the type to convert to
     * @param fromType      the type to convert from
     * @param typeConverter the type converter to use
     */
    void addTypeConverter(Class<?> toType, Class<?> fromType, TypeConverter typeConverter);

    /**
     * Registers a new type converter.
     * <p/>
     * This method may throw {@link TypeConverterExistsException} if configured to fail if an existing
     * type converter already exists
     *
     * @param targetType        the type to convert to
     * @param sourceType      the type to convert from
     * @param typeConverter the type converter to use
     */
    void addTypeConverter(String targetType, String sourceType, TypeConverter typeConverter);

    /**
     * Removes the type converter
     *
     * @param toType        the type to convert to
     * @param fromType      the type to convert from
     * @return <tt>true</tt> if removed, <tt>false</tt> if the type converter didn't exist
     */
    boolean removeTypeConverter(Class<?> toType, Class<?> fromType);

    /**
     * Performs a lookup for a given type converter.
     *
     * @param toType        the type to convert to
     * @param fromType      the type to convert from
     * @return the type converter or <tt>null</tt> if not found.
     */
    TypeConverter lookup(String toType, String fromType);

}
