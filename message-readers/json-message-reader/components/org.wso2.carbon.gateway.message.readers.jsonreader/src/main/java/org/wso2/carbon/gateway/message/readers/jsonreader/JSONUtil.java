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

package org.wso2.carbon.gateway.message.readers.jsonreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A Util class which is used for JSON Processing
 */
public class JSONUtil {

    /**
     * An Un-closable, Read-Only, Reusable, BufferedInputStream
     */
    static class ReadOnlyBIS extends BufferedInputStream {
        private static final Logger logger = LoggerFactory.getLogger(ReadOnlyBIS.class);

        public ReadOnlyBIS(InputStream inputStream) {
            super(inputStream);
            super.mark(Integer.MAX_VALUE);
            if (logger.isDebugEnabled()) {
                logger.debug("<init>");
            }
        }

        @Override
        public void close() throws IOException {
            super.reset();
            //super.mark(Integer.MAX_VALUE);
            if (logger.isDebugEnabled()) {
                logger.debug("#close");
            }
        }

        @Override
        public void mark(int readlimit) {
            if (logger.isDebugEnabled()) {
                logger.debug("#mark");
            }
        }

        @Override
        public boolean markSupported() {
            return true; //but we don't mark.
        }

        @Override
        public long skip(long n) {
            if (logger.isDebugEnabled()) {
                logger.debug("#skip");
            }
            return 0;
        }
    }

    public static InputStream toReadOnlyStream(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        return new ReadOnlyBIS(inputStream);
    }

}
