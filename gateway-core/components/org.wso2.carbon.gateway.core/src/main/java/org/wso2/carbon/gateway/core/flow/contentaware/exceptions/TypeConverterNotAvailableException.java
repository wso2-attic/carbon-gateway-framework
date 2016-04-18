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

package org.wso2.carbon.gateway.core.flow.contentaware.exceptions;

/**
 * An exception thrown if a value could not be converted to the required type
 */
public class TypeConverterNotAvailableException extends Exception {

    private static final long serialVersionUID = -8721487434390572636L;
//    private final Object value;
//    private final Class<?> type;

    public TypeConverterNotAvailableException(String from, String to) {
        super(createMessage(from, to));
    }

    public TypeConverterNotAvailableException(String from, String to, Throwable cause) {
        super(createMessage(from, to, cause), cause);
    }

    /**
     * Returns an error message for no type converter available.
     */
    public static String createMessage(String from, String to) {
        return "No type converter available to convert from type: " +
               from + " to the required type: " + to;
    }

    /**
     * Returns an error message for no type converter available with the cause.
     */
    public static String createMessage(String from, String to, Throwable cause) {
        return "Converting Exception when converting from type: " +
               from + " to the required type: " + to +
               ", which is caused by " + cause;
    }
}

