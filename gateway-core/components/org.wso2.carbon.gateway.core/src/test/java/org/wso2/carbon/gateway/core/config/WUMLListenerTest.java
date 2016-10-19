package org.wso2.carbon.gateway.core.config;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.gateway.core.config.dsl.external.deployer.IFlowDeployer;
import org.wso2.carbon.gateway.core.config.dsl.external.wuml.WUMLBaseListenerImpl;
import org.wso2.carbon.gateway.core.config.dsl.external.wuml.generated.WUMLLexer;
import org.wso2.carbon.gateway.core.config.dsl.external.wuml.generated.WUMLParser;
import org.wso2.carbon.gateway.core.flow.Mediator;
import org.wso2.carbon.gateway.core.flow.MediatorCollection;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.FilterMediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.invokers.CallMediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.invokers.RespondMediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.manipulators.PropertyMediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.manipulators.log.LogMediator;
import org.wso2.carbon.gateway.core.inbound.Dispatcher;
import org.wso2.carbon.gateway.core.inbound.InboundEPDeployer;
import org.wso2.carbon.gateway.core.inbound.InboundEPProviderRegistry;
import org.wso2.carbon.gateway.core.inbound.InboundEndpoint;
import org.wso2.carbon.gateway.core.inbound.Provider;
import org.wso2.carbon.gateway.core.outbound.OutboundEPProvider;
import org.wso2.carbon.gateway.core.outbound.OutboundEPProviderRegistry;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Unit test case for WUMLListenerImpl
 */
public class WUMLListenerTest {

    @Before
    public void setup() {
        InboundEPProviderRegistry.getInstance().registerInboundEPProvider(new TestProvider());
        OutboundEPProviderRegistry.getInstance().registerOutboundEPProvider(new TestOutboundEPProvider());
    }

    @Ignore
    @Test()
    public void testListernerConstantsIflow() {
        Assert.assertTrue((parseIflow("/integration-flows/constants.iflow")));
    }

    @Ignore
    @Test
    public void testListernerVariableAssignmentIflow() {
        Assert.assertTrue((parseIflow("/integration-flows/variable-assignment.iflow")));
    }

    @Test
    public void testPassThroughObjectModelCreation() {
        String iFlowResource = "/integration-flows/passthrough.xyz";
        String integrationName = "passthrough";
        String resourceName = "passthrough";
        Boolean asExpected;

        Assert.assertTrue("Error while deploying config :" + iFlowResource, deployArtifacts(iFlowResource));

        Integration sampleIntegration = IntegrationConfigRegistry.getInstance().getIntegrationConfig(integrationName);
        MediatorCollection generatedMediatorCollection = sampleIntegration.getResource(resourceName).getDefaultWorker()
                .getMediators();
        List<Mediator> mediatorList = generatedMediatorCollection.getMediators();

        // Evaluate the generated mediator collection
        if (mediatorList.size() == 3) {
            asExpected = (generatedMediatorCollection.getFirstMediator() instanceof PropertyMediator);
            asExpected = (mediatorList.get(1) instanceof CallMediator) && asExpected;
            asExpected = (mediatorList.get(2) instanceof RespondMediator) && asExpected;
        } else {
            asExpected = false;
        }

        // remove the created Integration
        IntegrationConfigRegistry.getInstance().removeIntegrationConfig(sampleIntegration);

        Assert.assertTrue("Object generation according to the provided NEL configuration has failed.", asExpected);
    }

    @Test
    public void testFilterMediatorObjectCreation() {
        String iFlowResource = "/integration-flows/filter.xyz";
        String integrationName = "filter";
        String resourceName = "passthrough";
        Boolean asExpected;

        Assert.assertTrue("Error while deploying config :" + iFlowResource, deployArtifacts(iFlowResource));

        Integration sampleIntegration = IntegrationConfigRegistry.getInstance().getIntegrationConfig(integrationName);
        MediatorCollection generatedMediatorCollection = sampleIntegration.getResource(resourceName).getDefaultWorker()
                .getMediators();
        List<Mediator> mediatorList = generatedMediatorCollection.getMediators();

        // Evaluate the generated mediator collection
        if (mediatorList.size() == 6) {
            asExpected = (generatedMediatorCollection.getFirstMediator() instanceof PropertyMediator);
            asExpected = (mediatorList.get(1) instanceof LogMediator) && asExpected;
            asExpected = (mediatorList.get(2) instanceof FilterMediator) && asExpected;
            asExpected = (mediatorList.get(3) instanceof LogMediator) && asExpected;
            asExpected = (mediatorList.get(4) instanceof CallMediator) && asExpected;
            asExpected = (mediatorList.get(5) instanceof RespondMediator) && asExpected;

            // expand the filter mediator
            List<Mediator> thenMediatorList = ((FilterMediator) mediatorList.get(2)).getChildThenMediatorList()
                    .getMediators();
            List<Mediator> elseMediatorList = ((FilterMediator) mediatorList.get(2)).getChildOtherwiseMediatorList()
                    .getMediators();

            // expand the filter mediator inside if-then block
            asExpected = (((FilterMediator) thenMediatorList.get(0)).getChildThenMediatorList()
                    .getFirstMediator() instanceof LogMediator) && asExpected;
            asExpected = (((FilterMediator) thenMediatorList.get(0)).getChildOtherwiseMediatorList()
                    .getFirstMediator() instanceof LogMediator) && asExpected;

            // expand the filter mediator inside else block
            asExpected = (((FilterMediator) elseMediatorList.get(0)).getChildThenMediatorList()
                    .getFirstMediator() instanceof LogMediator) && asExpected;
            asExpected = (((FilterMediator) elseMediatorList.get(0)).getChildOtherwiseMediatorList()
                    .getFirstMediator() instanceof LogMediator) && asExpected;
        } else {
            asExpected = false;
        }

        // remove the created Integration
        IntegrationConfigRegistry.getInstance().removeIntegrationConfig(sampleIntegration);

        Assert.assertTrue("Object generation according to the provided NEL configuration has failed.", asExpected);
    }

    private boolean deployArtifacts(String iFlowResource) {
        Artifact testArtifact = new Artifact(new File(getClass().getResource(iFlowResource).getFile()));
        IFlowDeployer iFlowDeployer = new IFlowDeployer();
        try {
            iFlowDeployer.deploy(testArtifact);
        } catch (CarbonDeploymentException e) {
            return false;
        }
        return true;
    }

    private boolean parseIflow(String iFlowResource) {
        WUMLLexer lexer;
        WUMLParser parser;
        WUMLBaseListenerImpl wumlBaseListener = new WUMLBaseListenerImpl("Test1");

        InputStream inputStream = null;
        File file = null;
        try {
            file = new File(getClass().getResource(iFlowResource).getFile());
            inputStream = new FileInputStream(file);

            CharStream cs = new ANTLRInputStream(inputStream);

            // Passing the input to the lexer to create tokens
            lexer = new WUMLLexer(cs);

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // Passing the tokens to the parser to create the parse trea.
            parser = new WUMLParser(tokens);
            parser.addParseListener(wumlBaseListener);
            parser.sourceFile();

            // TODO: Need to update the test case with IntegrationConfigRegistry object.

            return true;

        } catch (IOException e) {
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    return false;
                }
            }
        }
    }

    class TestProvider implements Provider {

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
            return new TestInboundEndpoint();
        }

        @Override
        public Dispatcher getInboundEndpointDispatcher() {
            return null;
        }
    }

    class TestInboundEndpoint extends InboundEndpoint {

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

    class TestOutboundEPProvider implements OutboundEPProvider {

        @Override
        public String getProtocol() {
            return "http";
        }

        @Override
        public OutboundEndpoint getEndpoint() {
            return new OutboundEndpoint();
        }
    }

    class OutboundEndpoint implements org.wso2.carbon.gateway.core.outbound.OutboundEndpoint {

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
