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
package org.wso2.ballerina.core.config.annotations.resource.http.methods;

import org.wso2.ballerina.core.config.ConfigConstants;
import org.wso2.ballerina.core.config.annotations.BooleanAnnotation;

/**
 *
 */
public class Put extends BooleanAnnotation {
    public Put() {
        super(ConfigConstants.PUT_ANNOTATION, Boolean.FALSE);
    }

    public Put(Boolean value) {
        super(ConfigConstants.PUT_ANNOTATION, value);
    }
}
