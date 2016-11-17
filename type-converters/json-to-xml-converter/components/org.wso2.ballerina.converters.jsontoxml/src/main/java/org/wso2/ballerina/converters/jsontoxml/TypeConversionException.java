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

package org.wso2.ballerina.converters.jsontoxml;

/**
 * Exception when failing during type conversion.
 *
 * @version
 */
public class TypeConversionException extends Exception {
    private static final long serialVersionUID = -6118520819865759886L;

    private final Object value;
    private final Class<?> type;

    public TypeConversionException(Object value, Class<?> type, Throwable cause) {
        super(createMessage(value, type, cause), cause);
        this.value = value;
        this.type = type;
    }

    /**
     * Returns the value which could not be converted
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the required <tt>to</tt> type
     */
    public Class<?> getToType() {
        return type;
    }

    /**
     * Returns the required <tt>from</tt> type.
     * Returns <tt>null</tt> if the provided value was null.
     */
    public Class<?> getFromType() {
        if (value != null) {
            return value.getClass();
        } else {
            return null;
        }
    }

    /**
     * Returns an error message for type conversion failed.
     */
    public static String createMessage(Object value, Class<?> type, Throwable cause) {
        return "Error during type conversion from type: " + (value != null ? value.getClass().getCanonicalName() : null)
                + " to the required type: " + type.getCanonicalName() + " with value " + value + " due " + cause
                .getMessage();
    }

}


