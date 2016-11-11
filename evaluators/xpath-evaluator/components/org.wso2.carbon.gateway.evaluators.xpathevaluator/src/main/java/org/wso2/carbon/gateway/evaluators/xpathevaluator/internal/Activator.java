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

package org.wso2.carbon.gateway.evaluators.xpathevaluator.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.flow.contentaware.abstractcontext.MessageBodyEvaluator;
import org.wso2.carbon.gateway.evaluators.xpathevaluator.XPathEvaluator;

/**
 * OSGi BundleActivator of the XPathEvaluator.
 */
public class Activator implements BundleActivator {
    private static final Logger log = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Activating the bundle registering " + XPathEvaluator.class + " service");
        }
        bundleContext.registerService(MessageBodyEvaluator.class, new XPathEvaluator(), null);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        // TODO: Well we should be unregistering the service but under normal conditions bundle shouldn't stop.
        // So, should we unregister and also put some warning? Whats the best practice?
        // In production servers will be running in daemon mode and there won't be
        // an OSGi console as well to stop bundles from there
    }
}
