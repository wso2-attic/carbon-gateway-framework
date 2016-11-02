/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.gateway.core.worker.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * JAXB representation of the thread pool configuration
 */
@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.FIELD)
public class ThreadPoolConfiguration {

    public static final int VAL = 20;

    public static ThreadPoolConfiguration getDefault() {
        ThreadPoolConfiguration defaultConfig;
        defaultConfig = new ThreadPoolConfiguration(VAL);
        return defaultConfig;
    }

    @XmlAttribute(required = true)
    private int noOfThreads;

    @XmlAttribute(required = true)
    private String enable;

    public ThreadPoolConfiguration() {

    }

    public ThreadPoolConfiguration(int val) {
        this.noOfThreads = val;
    }


    public int getNoOfThreads() {
        return noOfThreads;
    }

    public void setNoOfThreads(int noOfThreads) {
        this.noOfThreads = noOfThreads;
    }

    public boolean isEnable() {
        if (enable == null) {
            return false;
        }
        return Boolean.parseBoolean(enable);
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }
}
