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

import org.wso2.carbon.gateway.core.flow.contentAwareSupport.exceptions.TypeConversionException;

import java.io.IOException;
import java.io.InputStream;

/**
 * This implements different type converters
 */
public interface TypeConverter {


    /**
     * Converts the value to the specified type
     *
     * @param inputStream the inputStream of cMsg
     * @return the converted value, or <tt>null</tt> if not possible to convert
     * @throws TypeConversionException is thrown if error during type conversion
     */
    InputStream convert(InputStream inputStream) throws TypeConversionException, IOException;

    /**
     * Converts the value to the specified type
     *
     * @param anyValue any type of an object
     * @return the converted value, or <tt>null</tt> if not possible to convert
     * @throws TypeConversionException is thrown if error during type conversion
     */
    <T> T convert(Object anyValue) throws TypeConversionException;

    String getSourceType();

    String getTargetType();

    }
