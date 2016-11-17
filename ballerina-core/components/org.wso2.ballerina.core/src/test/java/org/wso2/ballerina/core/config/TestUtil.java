/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.config.dsl.external.deployer.IFlowDeployer;
import org.wso2.ballerina.core.inbound.Dispatcher;
import org.wso2.ballerina.core.inbound.InboundEPDeployer;
import org.wso2.ballerina.core.inbound.InboundEndpoint;
import org.wso2.ballerina.core.inbound.Provider;
import org.wso2.ballerina.core.outbound.OutboundEPProvider;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Class containing Util methods for Parser Testing
 */
public class TestUtil {
    private static final Logger log = LoggerFactory.getLogger(TestUtil.class);

    /**
     * Deploy a given configuration file to the Ballerina
     *
     * @param testArtifact Test Artifact containing the configuration file
     * @return true if the deployment was successful, false otherwise
     */
    public static boolean deployArtifacts(Artifact testArtifact) {
        IFlowDeployer iFlowDeployer = new IFlowDeployer();
        try {
            iFlowDeployer.deploy(testArtifact);
        } catch (CarbonDeploymentException e) {
            log.error("Deployment failed for the configuration: " + testArtifact.getName(), e);
            return false;
        }
        return true;
    }

    static class TestProvider implements Provider {

        @Override
        public String getProtocol() {
            return "http";
        }

        @Override
        public InboundEPDeployer getInboundDeployer() {
            return null;
        }

        @Override
        public InboundEndpoint getInboundEndpoint() {
            return new TestUtil.TestInboundEndpoint();
        }

        @Override
        public Dispatcher getInboundEndpointDispatcher() {
            return null;
        }
    }

    static class TestInboundEndpoint extends InboundEndpoint {

        @Override
        public boolean canReceive(CarbonMessage cMsg) {
            return false;
        }

        @Override
        public String getProtocol() {
            return "http";
        }

        @Override
        public void setParameters(ParameterHolder parameters) {
        }
    }

    static class TestOutboundEPProvider implements OutboundEPProvider {

        @Override
        public String getProtocol() {
            return "http";
        }

        @Override
        public TestUtil.OutboundEndpoint getEndpoint() {
            return new TestUtil.OutboundEndpoint();
        }
    }

    static class OutboundEndpoint implements org.wso2.ballerina.core.outbound.OutboundEndpoint {

        @Override
        public int getTimeOut() {
            return 0;
        }

        @Override
        public void setTimeOut(int timeOut) {
        }

        @Override
        public String getName() {
            return "http";
        }

        @Override
        public void setName(String name) {
        }

        @Override
        public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
            return false;
        }

        @Override
        public void setParameters(ParameterHolder parameters) {
        }

        @Override
        public String getUri() {
            return null;
        }

        @Override
        public void setUri(String uri) {

        }
    }
}
