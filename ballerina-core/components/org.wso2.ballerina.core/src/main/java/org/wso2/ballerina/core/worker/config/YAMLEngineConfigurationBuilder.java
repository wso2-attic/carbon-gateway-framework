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

package org.wso2.ballerina.core.worker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * A Configuration builder class for engine.Read the .yml file and load engine properties
 */
public class YAMLEngineConfigurationBuilder {

    public static final String BALLERINA_ENGINE_CONF = "ballerina.engine.conf";

    private static final Logger log = LoggerFactory.getLogger(YAMLEngineConfigurationBuilder.class);

    public static ThreadModelConfiguration build() {
        ThreadModelConfiguration threadModelConfiguration;
        String nettyTransportsConfigFile = System.getProperty(BALLERINA_ENGINE_CONF,
                "conf" + File.separator + "engine" + File.separator + "engine-config.yml");

        File file = new File(nettyTransportsConfigFile);
        //If file exists load file
        if (file.exists() && file.canRead()) {
            log.debug("Loading file " + nettyTransportsConfigFile);
            Reader in = null;
            try {
                in = new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1);
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                threadModelConfiguration = yaml.loadAs(in, ThreadModelConfiguration.class);
            } catch (IOException e) {
                String msg = "Error while loading " + nettyTransportsConfigFile + " configuration file";
                throw new RuntimeException(msg, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error("Cannot close opened stream ", e);
                    }
                }
            }
        } else {
            log.info("Cannot find engine level property file hence using default configs");
            // return a default config
            threadModelConfiguration = ThreadModelConfiguration.getDefault();
        }
        return threadModelConfiguration;
    }
}
