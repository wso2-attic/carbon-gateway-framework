package org.wso2.carbon.gateway.core.config;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
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
import org.wso2.carbon.gateway.core.inbound.InboundEPProviderRegistry;
import org.wso2.carbon.gateway.core.outbound.OutboundEPProviderRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Unit test case for WUMLListenerImpl
 */
public class WUMLListenerTest {

    private static final Logger log = LoggerFactory.getLogger(WUMLListenerTest.class);


    @Before
    public void setup() {
        InboundEPProviderRegistry.getInstance().registerInboundEPProvider(new TestUtil.TestProvider());
        OutboundEPProviderRegistry.getInstance().registerOutboundEPProvider(new TestUtil.TestOutboundEPProvider());
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

    /**
     * Testing whether the correct order of mediator objects are created according to the passthrough.xyz configuration
     */
    @Test
    public void testPassThroughObjectModelCreation() {
        String iFlowResource = "/integration-flows/passthrough.iflow";
        String integrationName = "passthrough";
        String resourceName = "passthrough";

        Artifact testArtifact = new Artifact(new File(getClass().getResource(iFlowResource).getFile()));
        Assert.assertTrue("Error while deploying config :" + iFlowResource, TestUtil.deployArtifacts(testArtifact));

        Integration sampleIntegration = IntegrationConfigRegistry.getInstance().getIntegrationConfig(integrationName);
        MediatorCollection generatedMediatorCollection = sampleIntegration.getResource(resourceName).getDefaultWorker()
                .getMediators();
        List<Mediator> mediatorList = generatedMediatorCollection.getMediators();

        // Evaluate the generated mediator collection
        Assert.assertTrue("Number of generated mediator objects is incorrect.", mediatorList.size() == 3);

        Assert.assertTrue("First mediator is not a Property mediator.",
                (generatedMediatorCollection.getFirstMediator() instanceof PropertyMediator));
        Assert.assertTrue("Second mediator is not a Call mediator.", (mediatorList.get(1) instanceof CallMediator));
        Assert.assertTrue("Third mediator is not a Respond mediator.",
                (mediatorList.get(2) instanceof RespondMediator));

        // remove the created Integration
        IntegrationConfigRegistry.getInstance().removeIntegrationConfig(sampleIntegration);
    }

    /**
     * Testing whether the correct order of mediator objects are created according to the filter.xyz configuration
     */
    @Test
    public void testFilterMediatorObjectCreation() {
        String iFlowResource = "/integration-flows/filter.iflow";
        String integrationName = "filter";
        String resourceName = "passthrough";

        Artifact testArtifact = new Artifact(new File(getClass().getResource(iFlowResource).getFile()));
        Assert.assertTrue("Error while deploying config :" + iFlowResource, TestUtil.deployArtifacts(testArtifact));

        Integration sampleIntegration = IntegrationConfigRegistry.getInstance().getIntegrationConfig(integrationName);
        MediatorCollection generatedMediatorCollection = sampleIntegration.getResource(resourceName).getDefaultWorker()
                .getMediators();
        List<Mediator> mediatorList = generatedMediatorCollection.getMediators();

        // Evaluate the generated mediator collection
        Assert.assertTrue("Number of generated mediator objects is incorrect.", mediatorList.size() == 6);

        Assert.assertTrue("Mediator object is not a Property mediator instance as expected. ",
                (generatedMediatorCollection.getFirstMediator() instanceof PropertyMediator));
        Assert.assertTrue("Mediator object is not a Log mediator instance as expected. ",
                (mediatorList.get(1) instanceof LogMediator));
        Assert.assertTrue("Mediator object is not a Filter mediator instance as expected. ",
                (mediatorList.get(2) instanceof FilterMediator));
        Assert.assertTrue("Mediator object is not a Log mediator instance as expected. ",
                (mediatorList.get(3) instanceof LogMediator));
        Assert.assertTrue("Mediator object is not a Call mediator instance as expected. ",
                (mediatorList.get(4) instanceof CallMediator));
        Assert.assertTrue("Mediator object is not a Respond mediator instance as expected. ",
                (mediatorList.get(5) instanceof RespondMediator));

        // expand the filter mediator
        List<Mediator> thenMediatorList = ((FilterMediator) mediatorList.get(2)).getChildThenMediatorList()
                .getMediators();
        List<Mediator> elseMediatorList = ((FilterMediator) mediatorList.get(2)).getChildOtherwiseMediatorList()
                .getMediators();

        // expand the filter mediator inside if-then block
        Assert.assertTrue("Mediator object is not a Log mediator instance as expected. ",
                (((FilterMediator) thenMediatorList.get(0)).getChildThenMediatorList()
                        .getFirstMediator() instanceof LogMediator));
        Assert.assertTrue("Mediator object is not a Log mediator instance as expected. ",
                (((FilterMediator) thenMediatorList.get(0)).getChildOtherwiseMediatorList()
                        .getFirstMediator() instanceof LogMediator));

        // expand the filter mediator inside else block
        Assert.assertTrue("Mediator object is not a Log mediator instance as expected. ",
                (((FilterMediator) elseMediatorList.get(0)).getChildThenMediatorList()
                        .getFirstMediator() instanceof LogMediator));
        Assert.assertTrue("Mediator object is not a Log mediator instance as expected. ",
                (((FilterMediator) elseMediatorList.get(0)).getChildOtherwiseMediatorList()
                        .getFirstMediator() instanceof LogMediator));

        // remove the created Integration
        IntegrationConfigRegistry.getInstance().removeIntegrationConfig(sampleIntegration);
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
}
