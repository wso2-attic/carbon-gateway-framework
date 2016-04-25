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

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * A Configuration builder class for engine
 */
public class YAMLEngineConfigurationBuilder {

    public static final String GATEWAY_ENGINE_CONF = "gateway.engine.conf";

    public static ThreadModelConfiguration build() {
        ThreadModelConfiguration threadModelConfiguration;
        String nettyTransportsConfigFile =
                   System.getProperty(GATEWAY_ENGINE_CONF,
                                      "conf" + File.separator + "engine" + File.separator + "engine-config.yml");

        File file = new File(nettyTransportsConfigFile);
        if (file.exists()) {
            try (Reader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1)) {
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                threadModelConfiguration = yaml.loadAs(in, ThreadModelConfiguration.class);
            } catch (IOException e) {
                String msg = "Error while loading " + nettyTransportsConfigFile + " configuration file";
                throw new RuntimeException(msg, e);
            }
        } else { // return a default config
            threadModelConfiguration = ThreadModelConfiguration.getDefault();
        }
        return threadModelConfiguration;
    }
}
