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

package org.wso2.carbon.gateway.core.config.dsl.external.deployer;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.gateway.core.config.Integration;
import org.wso2.carbon.gateway.core.config.IntegrationConfigRegistry;
import org.wso2.carbon.gateway.core.config.dsl.external.wuml.WUMLBaseListenerImpl;
import org.wso2.carbon.gateway.core.config.dsl.external.wuml.generated.WUMLLexer;
import org.wso2.carbon.gateway.core.config.dsl.external.wuml.generated.WUMLParser;
import org.wso2.carbon.gateway.core.inbound.ProviderRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.wso2.carbon.gateway.core.Constants.EMPTY_STRING;

/**
 * A class responsible for read the .iflow files and deploy them to the runtime Object model.
 */
@Component(
        name = "org.wso2.carbon.gateway.core.config.dsl.external.deployer.IFlowDeployer",
        immediate = true,
        service = Deployer.class)
public class IFlowDeployer implements Deployer {

    public static final String EXTERNAL_DSL_CONFIGS_DIRECTORY = "integration-flows";
    private static final String FILE_EXTENSION = ".ballerina";
    private static final Logger logger = LoggerFactory.getLogger(IFlowDeployer.class);
    private ArtifactType artifactType;
    private URL directoryLocation;

    @Activate
    protected void activate(BundleContext bundleContext) {
        //TODO: check whether this guarantee whether this deployer registered on
        //TODO: after all mandatory services are available
    }

    @Reference(
            name = "inbound-provider-registry-service",
            service = ProviderRegistry.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeInboundProviderRegistry")
    protected void addInboundProviderRegistry(ProviderRegistry registry) {
    }

    protected void removeInboundProviderRegistry(ProviderRegistry registry) {
    }

    @Reference(
            name = "outbound-provider-registry-service",
            service = org.wso2.carbon.gateway.core.outbound.ProviderRegistry.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeOutboundProviderRegistry")
    protected void addOutboundProviderRegistry(org.wso2.carbon.gateway.core.outbound.ProviderRegistry registry) {
    }

    protected void removeOutboundProviderRegistry(org.wso2.carbon.gateway.core.outbound.ProviderRegistry registry) {
    }

    @Reference(
            name = "mediator-provider-registry-service",
            service = org.wso2.carbon.gateway.core.flow.ProviderRegistry.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeMediatorProviderRegistry")
    protected void addMediatorProviderRegistry(org.wso2.carbon.gateway.core.flow.ProviderRegistry registry) {
    }

    protected void removeMediatorProviderRegistry(org.wso2.carbon.gateway.core.flow.ProviderRegistry registry) {
    }

    @Override
    public void init() {
        artifactType = new ArtifactType<String>("iflow");
        try {
            directoryLocation = new URL("file:" + EXTERNAL_DSL_CONFIGS_DIRECTORY);
        } catch (MalformedURLException e) {
            logger.error("Error while initializing directoryLocation" + directoryLocation.getPath(), e);
        }
    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        logger.info("Deploying artifact " + artifact.getFile().getName());
        updateESBConfig(artifact);
        return artifact.getFile().getName();
    }

    @Override
    public void undeploy(Object o) throws CarbonDeploymentException {
        logger.info("Undeploying artifact " + (String) o);
        Integration configHolder = IntegrationConfigRegistry.getInstance()
                .getIntegrationConfig(((String) o).replace(FILE_EXTENSION, EMPTY_STRING));
        if (configHolder != null) {
            IntegrationConfigRegistry.getInstance().removeIntegrationConfig(configHolder);
        }
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        logger.info("Updating artifact " + artifact.getFile().getName());
        undeploy(artifact.getKey());
        updateESBConfig(artifact);
        return artifact.getFile().getName();
    }

    @Override
    public URL getLocation() {
        return directoryLocation;
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }

    private void updateESBConfig(Artifact artifact) {

        InputStream inputStream = null;
        try {
            File file = artifact.getFile();
            inputStream = new FileInputStream(file);

            if (file.getName().endsWith(FILE_EXTENSION)) {
                String integrationName = file.getName().replace(FILE_EXTENSION, EMPTY_STRING);

                logger.info("Deploying Integration : " + integrationName);

                CharStream cs = new ANTLRInputStream(inputStream);

                // Passing the input to the lexer to create tokens
                WUMLLexer lexer = new WUMLLexer(cs);

                CommonTokenStream tokens = new CommonTokenStream(lexer);

                // Passing the tokens to the parser to create the parse trea.
                WUMLParser parser = new WUMLParser(tokens);

                // Adding the listener to facilitate walking through parse tree.
                WUMLBaseListenerImpl wumlBaseListener = new WUMLBaseListenerImpl(integrationName);

                parser.addParseListener(wumlBaseListener);
                parser.sourceFile();

            }
        } catch (IOException e) {
            logger.error("Error while creating Cheetah object model", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("Error while closing the input stream", e);
                }
            }
        }
    }

}
