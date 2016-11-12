package org.wso2.carbon.gateway.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.gateway.core.config.dsl.external.deployer.IFlowDeployer;

/**
 * Class containing Util methods for Parser Testing
 */
public class TestUtil {
    private static final Logger log = LoggerFactory.getLogger(TestUtil.class);

    /**
     * Deploy a given configuration file to the Gateway
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
}
