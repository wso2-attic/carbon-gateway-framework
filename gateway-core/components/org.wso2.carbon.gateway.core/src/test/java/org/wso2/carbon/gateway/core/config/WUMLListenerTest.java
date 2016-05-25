package org.wso2.carbon.gateway.core.config;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.wso2.carbon.gateway.core.config.dsl.external.WUMLConfigurationBuilder;
import org.wso2.carbon.gateway.core.config.dsl.external.wuml.WUMLBaseListenerImpl;
import org.wso2.carbon.gateway.core.config.dsl.external.wuml.generated.WUMLLexer;
import org.wso2.carbon.gateway.core.config.dsl.external.wuml.generated.WUMLParser;

import org.wso2.carbon.gateway.core.inbound.Dispatcher;
import org.wso2.carbon.gateway.core.inbound.InboundEPDeployer;
import org.wso2.carbon.gateway.core.inbound.InboundEPProviderRegistry;
import org.wso2.carbon.gateway.core.inbound.InboundEndpoint;
import org.wso2.carbon.gateway.core.inbound.Provider;

import org.wso2.carbon.gateway.core.outbound.OutboundEPProvider;
import org.wso2.carbon.gateway.core.outbound.OutboundEPProviderRegistry;
import org.wso2.carbon.gateway.core.outbound.OutboundEndpoint;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Unit test case for WUMLListenerImpl
 */
public class WUMLListenerTest {

    @Before
    public void setup() {
        InboundEPProviderRegistry.getInstance().registerInboundEPProvider(new TestProvider());
        OutboundEPProviderRegistry.getInstance().registerOutboundEPProvider(new TestOutboundEPProvider());
    }

    @Test
    public void testListernerConstantsIflow() {
        Assert.assertTrue((parseIflow("/integration-flows/constants.iflow")));
    }

    @Test
    public void testListernerVariableAssignmentIflow() {
        Assert.assertTrue((parseIflow("/integration-flows/variable-assignment.iflow")));
    }


    private boolean parseIflow(String iFlowResource) {
        WUMLLexer lexer;
        WUMLParser parser;
        WUMLBaseListenerImpl wumlBaseListener = new WUMLBaseListenerImpl();

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
            parser.script();

            WUMLConfigurationBuilder.IntegrationFlow integrationFlow = wumlBaseListener.getIntegrationFlow();
            GWConfigHolder configHolder = integrationFlow.getGWConfigHolder();
            if (configHolder != null) {
                ConfigRegistry.getInstance().addGWConfig(configHolder);
            }

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
        public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
            return false;
        }

        @Override
        public void setParameters(ParameterHolder parameters) {
        }

        @Override
        public void setName(String name) {
        }
    }
}
