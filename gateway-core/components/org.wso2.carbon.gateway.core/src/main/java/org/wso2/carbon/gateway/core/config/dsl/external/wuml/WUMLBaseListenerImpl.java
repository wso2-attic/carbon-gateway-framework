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
package org.wso2.carbon.gateway.core.config.dsl.external.wuml;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.gateway.core.Constants;
import org.wso2.carbon.gateway.core.config.ConfigConstants;
import org.wso2.carbon.gateway.core.config.Integration;
import org.wso2.carbon.gateway.core.config.IntegrationConfigRegistry;
import org.wso2.carbon.gateway.core.config.Parameter;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.config.dsl.external.StringParserUtil;
import org.wso2.carbon.gateway.core.config.dsl.external.WUMLConfigurationBuilder;
import org.wso2.carbon.gateway.core.config.dsl.external.wuml.generated.WUMLBaseListener;
import org.wso2.carbon.gateway.core.config.dsl.external.wuml.generated.WUMLParser;
import org.wso2.carbon.gateway.core.exception.ChildExceptionHandler;
import org.wso2.carbon.gateway.core.exception.ConnectionClosedExceptionHandler;
import org.wso2.carbon.gateway.core.exception.ConnectionFailedExceptionHandler;
import org.wso2.carbon.gateway.core.exception.ConnectionTimeoutExceptionHandler;
import org.wso2.carbon.gateway.core.exception.GeneralExceptionHandler;
import org.wso2.carbon.gateway.core.flow.AbstractFlowController;
import org.wso2.carbon.gateway.core.flow.Mediator;
import org.wso2.carbon.gateway.core.flow.MediatorProviderRegistry;
import org.wso2.carbon.gateway.core.flow.Resource;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.Condition;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.FilterMediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.Source;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.TryBlockMediator;
import org.wso2.carbon.gateway.core.flow.templates.uri.URITemplate;
import org.wso2.carbon.gateway.core.flow.templates.uri.URITemplateException;
import org.wso2.carbon.gateway.core.inbound.InboundEPProviderRegistry;
import org.wso2.carbon.gateway.core.inbound.InboundEndpoint;
import org.wso2.carbon.gateway.core.outbound.OutboundEPProviderRegistry;
import org.wso2.carbon.gateway.core.outbound.OutboundEndpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Implementation class of the ANTLR generated listener class
 */
public class WUMLBaseListenerImpl extends WUMLBaseListener {
    private static final Logger log = LoggerFactory.getLogger(WUMLBaseListenerImpl.class);
    private Integration integration;
    private String integrationName;
    private Resource currentResource;
    // Temporary reference for the currently processing filter mediator
    private Stack<AbstractFlowController> flowControllerStack;
    private Stack<FlowControllerMediatorSection> flowControllerMediatorSection;

    public WUMLBaseListenerImpl(String fileName) {
        this.integrationName = fileName;
        this.flowControllerStack = new Stack<>();
        this.flowControllerMediatorSection = new Stack<>();
    }

    public WUMLConfigurationBuilder.IntegrationFlow getIntegrationFlow() {
        return null;
    }

    @Override
    public void enterSourceFile(WUMLParser.SourceFileContext ctx) {
        integration = new Integration(integrationName);
    }

    @Override
    public void exitSourceFile(WUMLParser.SourceFileContext ctx) {
        IntegrationConfigRegistry.getInstance().addIntegrationConfig(integration);
    }

    @Override
    public void enterDefinition(WUMLParser.DefinitionContext ctx) {
    }

    @Override
    public void exitDefinition(WUMLParser.DefinitionContext ctx) {
    }

    @Override
    public void enterConstants(WUMLParser.ConstantsContext ctx) {
    }

    @Override
    public void exitConstants(WUMLParser.ConstantsContext ctx) {
    }

    @Override
    public void enterResources(WUMLParser.ResourcesContext ctx) {
    }

    @Override
    public void exitResources(WUMLParser.ResourcesContext ctx) {
    }

    @Override
    public void enterPackageDef(WUMLParser.PackageDefContext ctx) {
    }

    @Override
    public void exitPackageDef(WUMLParser.PackageDefContext ctx) {
        //String packageName = ctx.qualifiedName().getText();
        /* Updating the integration name to its fully qualified name */
        //integration.setName(packageName + "." + integration.getName());
    }

    @Override
    public void enterPath(WUMLParser.PathContext ctx) {
    }

    @Override
    public void exitPath(WUMLParser.PathContext ctx) {
        integration.getAnnotation(ConfigConstants.AN_BASE_PATH)
                .setValue(StringParserUtil.getValueWithinDoubleQuotes(ctx.StringLiteral().getText()));
    }

    @Override
    public void enterSource(WUMLParser.SourceContext ctx) {
    }

    @Override
    public void exitSource(WUMLParser.SourceContext ctx) {
        Map<String, String> valueMap = new HashMap<>();
        ParameterHolder parameterHolder = new ParameterHolder();

        String host = StringParserUtil
                .getValueWithinDoubleQuotes(ctx.sourceElementValuePairs().host().StringLiteral().getText());
        String port = ctx.sourceElementValuePairs().port().IntegerLiteral().getText();
        String protocol = StringParserUtil
                .getValueWithinDoubleQuotes(ctx.sourceElementValuePairs().protocol().StringLiteral().getText());

        valueMap.put(Constants.HOST, host);
        valueMap.put(Constants.PORT, port);
        valueMap.put(Constants.PROTOCOL, protocol);
        integration.getAnnotation(ConfigConstants.AN_SOURCE).setValue(valueMap);

        parameterHolder.addParameter(new Parameter(Constants.HOST, host));
        parameterHolder.addParameter(new Parameter(Constants.PORT, port));
        parameterHolder.addParameter(new Parameter(Constants.CONTEXT,
                integration.getAnnotation(ConfigConstants.AN_BASE_PATH).getValue().toString()));

        InboundEndpoint inboundEndpoint = InboundEPProviderRegistry.getInstance().getProvider(protocol)
                .getInboundEndpoint();
        inboundEndpoint.setParameters(parameterHolder);
        integration.addInbound(inboundEndpoint);
    }

    @Override
    public void enterApi(WUMLParser.ApiContext ctx) {
    }

    @Override
    public void exitApi(WUMLParser.ApiContext ctx) {
    }

    @Override
    public void enterResourcePath(WUMLParser.ResourcePathContext ctx) {
    }

    @Override
    public void exitResourcePath(WUMLParser.ResourcePathContext ctx) {
    }

    @Override
    public void enterGetMethod(WUMLParser.GetMethodContext ctx) {
    }

    @Override
    public void exitGetMethod(WUMLParser.GetMethodContext ctx) {
    }

    @Override
    public void enterPostMethod(WUMLParser.PostMethodContext ctx) {
    }

    @Override
    public void exitPostMethod(WUMLParser.PostMethodContext ctx) {
    }

    @Override
    public void enterPutMethod(WUMLParser.PutMethodContext ctx) {
    }

    @Override
    public void exitPutMethod(WUMLParser.PutMethodContext ctx) {
    }

    @Override
    public void enterDeleteMethod(WUMLParser.DeleteMethodContext ctx) {
    }

    @Override
    public void exitDeleteMethod(WUMLParser.DeleteMethodContext ctx) {
    }

    @Override
    public void enterHeadMethod(WUMLParser.HeadMethodContext ctx) {
    }

    @Override
    public void exitHeadMethod(WUMLParser.HeadMethodContext ctx) {
    }

    @Override
    public void enterProdAnt(WUMLParser.ProdAntContext ctx) {
    }

    @Override
    public void exitProdAnt(WUMLParser.ProdAntContext ctx) {
    }

    @Override
    public void enterConAnt(WUMLParser.ConAntContext ctx) {
    }

    @Override
    public void exitConAnt(WUMLParser.ConAntContext ctx) {
    }

    @Override
    public void enterAntApiOperation(WUMLParser.AntApiOperationContext ctx) {
    }

    @Override
    public void exitAntApiOperation(WUMLParser.AntApiOperationContext ctx) {
    }

    @Override
    public void enterAntApiResponses(WUMLParser.AntApiResponsesContext ctx) {
    }

    @Override
    public void exitAntApiResponses(WUMLParser.AntApiResponsesContext ctx) {
    }

    @Override
    public void enterElementValuePairs(WUMLParser.ElementValuePairsContext ctx) {
    }

    @Override
    public void exitElementValuePairs(WUMLParser.ElementValuePairsContext ctx) {
    }

    @Override
    public void enterSourceElementValuePairs(WUMLParser.SourceElementValuePairsContext ctx) {
    }

    @Override
    public void exitSourceElementValuePairs(WUMLParser.SourceElementValuePairsContext ctx) {
    }

    @Override
    public void enterApiElementValuePairs(WUMLParser.ApiElementValuePairsContext ctx) {
    }

    @Override
    public void exitApiElementValuePairs(WUMLParser.ApiElementValuePairsContext ctx) {
    }

    @Override
    public void enterProtocol(WUMLParser.ProtocolContext ctx) {
    }

    @Override
    public void exitProtocol(WUMLParser.ProtocolContext ctx) {
    }

    @Override
    public void enterHost(WUMLParser.HostContext ctx) {
    }

    @Override
    public void exitHost(WUMLParser.HostContext ctx) {
    }

    @Override
    public void enterPort(WUMLParser.PortContext ctx) {
    }

    @Override
    public void exitPort(WUMLParser.PortContext ctx) {
    }

    @Override
    public void enterTags(WUMLParser.TagsContext ctx) {
    }

    @Override
    public void exitTags(WUMLParser.TagsContext ctx) {
    }

    @Override
    public void enterTag(WUMLParser.TagContext ctx) {
    }

    @Override
    public void exitTag(WUMLParser.TagContext ctx) {
    }

    @Override
    public void enterDescripton(WUMLParser.DescriptonContext ctx) {
    }

    @Override
    public void exitDescripton(WUMLParser.DescriptonContext ctx) {
    }

    @Override
    public void enterProducer(WUMLParser.ProducerContext ctx) {
    }

    @Override
    public void exitProducer(WUMLParser.ProducerContext ctx) {
    }

    @Override
    public void enterConstant(WUMLParser.ConstantContext ctx) {
    }

    @Override
    public void exitConstant(WUMLParser.ConstantContext ctx) {
        String type = (ctx.classType() != null) ? ctx.classType().getText() : ctx.type().getText();
        /* Extracting endpoints as constants */
        if (Constants.ENDPOINT.equals(type)) {
            String endpointType = ctx.getChild(5).getText();

            OutboundEndpoint outboundEndpoint = OutboundEPProviderRegistry.getInstance().getProvider(
                    endpointType.replace(Constants.ENDPOINT_GRAMMAR_KEYWORD, Constants.EMPTY_STRING)
                            .toLowerCase(Locale.ENGLISH)).getEndpoint();
            outboundEndpoint.setName(ctx.Identifier().get(0).getText());
            outboundEndpoint.setUri(StringParserUtil.getValueWithinDoubleQuotes(ctx.StringLiteral().getText()));
            integration.getOutbounds().put(ctx.Identifier().get(0).getText(), outboundEndpoint);
        }
    }

    @Override
    public void enterResource(WUMLParser.ResourceContext ctx) {
    }

    @Override
    public void exitResource(WUMLParser.ResourceContext ctx) {

        String path = StringParserUtil.getValueWithinDoubleQuotes(ctx.resourcePath().StringLiteral().getText());

        URITemplate uriTemplate = null;

        try {
            uriTemplate = new URITemplate(path);
        } catch (URITemplateException e) {
            log.error("Unable to create URI template for :" + path);
        }

        this.currentResource.setUritemplate(uriTemplate);

        /* Updating annotations */
        if (!ctx.httpMethods().getMethod().isEmpty()) {
            this.currentResource.getAnnotations().get(ConfigConstants.GET_ANNOTATION).setValue(Boolean.TRUE);
        }
        if (!ctx.httpMethods().putMethod().isEmpty()) {
            this.currentResource.getAnnotations().get(ConfigConstants.PUT_ANNOTATION).setValue(Boolean.TRUE);
        }
        if (!ctx.httpMethods().postMethod().isEmpty()) {
            this.currentResource.getAnnotations().get(ConfigConstants.POST_ANNOTATION).setValue(Boolean.TRUE);
        }
        if (!ctx.httpMethods().deleteMethod().isEmpty()) {
            this.currentResource.getAnnotations().get(ConfigConstants.DELETE_ANNOTATION).setValue(Boolean.TRUE);
        }
        this.currentResource.getAnnotations().get(ConfigConstants.AN_BASE_PATH).setValue(path);

        integration.getResources().put(this.currentResource.getName(), this.currentResource);
    }

    @Override
    public void enterHttpMethods(WUMLParser.HttpMethodsContext ctx) {
    }

    @Override
    public void exitHttpMethods(WUMLParser.HttpMethodsContext ctx) {
    }

    @Override
    public void enterQualifiedName(WUMLParser.QualifiedNameContext ctx) {
    }

    @Override
    public void exitQualifiedName(WUMLParser.QualifiedNameContext ctx) {
    }

    @Override
    public void enterResourceDeclaration(WUMLParser.ResourceDeclarationContext ctx) {
    }

    @Override
    public void exitResourceDeclaration(WUMLParser.ResourceDeclarationContext ctx) {
    }

    @Override
    public void enterElementValuePair(WUMLParser.ElementValuePairContext ctx) {
    }

    @Override
    public void exitElementValuePair(WUMLParser.ElementValuePairContext ctx) {
    }

    @Override
    public void enterElementValue(WUMLParser.ElementValueContext ctx) {
    }

    @Override
    public void exitElementValue(WUMLParser.ElementValueContext ctx) {
    }

    @Override
    public void enterBlock(WUMLParser.BlockContext ctx) {
    }

    @Override
    public void exitBlock(WUMLParser.BlockContext ctx) {
    }

    @Override
    public void enterBlockStatement(WUMLParser.BlockStatementContext ctx) {
    }

    @Override
    public void exitBlockStatement(WUMLParser.BlockStatementContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterParExpression(WUMLParser.ParExpressionContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterLocalVariableDeclarationStatement(WUMLParser.LocalVariableDeclarationStatementContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitLocalVariableDeclarationStatement(WUMLParser.LocalVariableDeclarationStatementContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterExpression(WUMLParser.ExpressionContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitExpression(WUMLParser.ExpressionContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterLiteral(WUMLParser.LiteralContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitLiteral(WUMLParser.LiteralContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterType(WUMLParser.TypeContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitType(WUMLParser.TypeContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterClassType(WUMLParser.ClassTypeContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitClassType(WUMLParser.ClassTypeContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterMediaType(WUMLParser.MediaTypeContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitMediaType(WUMLParser.MediaTypeContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void visitTerminal(TerminalNode node) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void visitErrorNode(ErrorNode node) {
    }

    @Override
    public void exitInvokeMediatorCall(WUMLParser.InvokeMediatorCallContext ctx) {
        /*  Implementation is done to directly call the backend and respond.
            Only supports when "return invoke(EP, message)"
            "message m = invoke(EP, message)" not supported
         */

        //Add the call mediator
        Mediator callMediator = MediatorProviderRegistry.getInstance().getMediator("call");

        ParameterHolder parameterHolder = new ParameterHolder();
        parameterHolder.addParameter(new Parameter("endpointKey", ctx.Identifier().get(0).getText()));

        callMediator.setParameters(parameterHolder);

        dropMediatorFilterAware(callMediator);
    }

    @Override
    public void exitResourceName(WUMLParser.ResourceNameContext ctx) {
        /* Creating the resource */
        this.currentResource = new Resource(ctx.Identifier().getText());
    }

    @Override
    public void exitLogMediatorCall(WUMLParser.LogMediatorCallContext ctx) {
        Mediator logMediator = MediatorProviderRegistry.getInstance().getMediator(Constants.LOG_MEDIATOR);
        ParameterHolder parameterHolder = new ParameterHolder();
        logMediator.setParameters(parameterHolder);
        //Currently the log only support for log full and custom string input
        if (ctx.literal() != null) {
            parameterHolder.addParameter(new Parameter("Log Message:", ctx.literal().getText()));
            parameterHolder.addParameter(new Parameter(Constants.LEVEL, Constants.LEVEL_CUSTOM));
        } else {
            parameterHolder.addParameter(new Parameter(Constants.LEVEL, Constants.LEVEL_FULL));
        }

        logMediator.setParameters(parameterHolder);
        dropMediatorFilterAware(logMediator);
    }

    /**
     * Handle custom mediator statements
     */
    @Override public void exitCustomMediatorCall(WUMLParser.CustomMediatorCallContext ctx) {
        /* Proceed only of the configuration is valid */
        if (ctx.getChildCount() == 7) {
            String mediatorName = ctx.Identifier().get(0).getText();
            String messageId = ctx.Identifier().get(1).getText();
            String configurations = StringParserUtil.getValueWithinDoubleQuotes(ctx.StringLiteral().getText());
            Mediator mediator = MediatorProviderRegistry.getInstance().getMediator(mediatorName);

            if (mediator != null) {
                ParameterHolder parameterHolder = new ParameterHolder();
                parameterHolder.addParameter(new Parameter("parameters", configurations));
                parameterHolder.addParameter(new Parameter("message", messageId));

                mediator.setParameters(parameterHolder);
                dropMediatorFilterAware(mediator);
            } else {
                log.warn("Mediator with the name :" + mediatorName + "not found.");
            }
        }
    }

    /**
     * Filter Mediator Handling
     */
    @Override
    public void enterIfElseBlock(WUMLParser.IfElseBlockContext ctx) {
    }

    @Override
    public void exitParExpression(WUMLParser.ParExpressionContext ctx) {
        //Condition condition = new Condition(source, Pattern.compile(conditionValue));
        // For expressions with pattern "exp1" == "exp2"
        if (ctx.EQUAL() != null) {
            List<WUMLParser.ExpressionContext> expressions = ((WUMLParser.ParExpressionContext) ctx).expression();
            // log.info(Integer.toString(expressions.size()));
            // Works only for formats: "eval("$p1.p2")=="abc""
            if (expressions != null && expressions.size() == 2) {
                String sourceDefinition = StringParserUtil
                        .getValueWithinDoubleQuotes(expressions.get(0).evalExpression().StringLiteral().getText());
                Source source = new Source(sourceDefinition);
                String conditionValue = StringParserUtil
                        .getValueWithinDoubleQuotes(expressions.get(1).literal().StringLiteral().getText());

                Condition condition = new Condition(source, Pattern.compile(conditionValue));

                FilterMediator filterMediator = new FilterMediator(condition);
                dropMediatorFilterAware(filterMediator);
                flowControllerStack.push(filterMediator);
                this.flowControllerMediatorSection.push(FlowControllerMediatorSection.ifBlock);
            }
        }
    }

    @Override
    public void exitIfElseBlock(WUMLParser.IfElseBlockContext ctx) {
        this.flowControllerStack.pop();
    }

    @Override
    public void enterIfBlock(WUMLParser.IfBlockContext ctx) {
    }

    @Override
    public void exitIfBlock(WUMLParser.IfBlockContext ctx) {
        this.flowControllerMediatorSection.pop();
    }

    @Override
    public void enterElseBlock(WUMLParser.ElseBlockContext ctx) {
        this.flowControllerMediatorSection.push(FlowControllerMediatorSection.elseBlock);
    }

    @Override
    public void exitElseBlock(WUMLParser.ElseBlockContext ctx) {
        this.flowControllerMediatorSection.pop();
    }


    /* Try-catch parsing */
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTryClause(WUMLParser.TryClauseContext ctx) {
        TryBlockMediator tryBlockMediator = new TryBlockMediator();
        dropMediatorFilterAware(tryBlockMediator);
        flowControllerStack.push(tryBlockMediator);
        this.flowControllerMediatorSection.push(FlowControllerMediatorSection.tryBlock);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTryClause(WUMLParser.TryClauseContext ctx) {
        this.flowControllerMediatorSection.pop();
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitExceptionHandler(WUMLParser.ExceptionHandlerContext ctx) {

        /* 0th element in the ctx is the exceptionType tag and 0th element of the of exceptionType tag
        * is the name of the exception. This conditional statement is applied to avoid user errors. */
        if (ctx.getChild(0) != null && ctx.getChild(0).getChild(0) != null) {

            String exceptionType = ((java.util.ArrayList) ((WUMLParser.ExceptionTypeContext) ctx.children
                    .get(0)).children).get(0).toString();

            ChildExceptionHandler childExceptionHandler = null;

            switch (exceptionType) {
            case Constants.CONN_CLOSED_EX:
                childExceptionHandler = new ConnectionClosedExceptionHandler();
                break;
            case Constants.CONN_FAILED_EX:
                childExceptionHandler = new ConnectionFailedExceptionHandler();
                break;
            case Constants.CONN_TIMEOUT_EX:
                childExceptionHandler = new ConnectionTimeoutExceptionHandler();
                break;
            case Constants.DEFAULT_EX:
                childExceptionHandler = new GeneralExceptionHandler();
                break;
            default:
                break;
            }
            ((TryBlockMediator) flowControllerStack.peek()).pushHandler(childExceptionHandler);
            this.flowControllerMediatorSection.push(FlowControllerMediatorSection.catchBlock);
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCatchClause(WUMLParser.CatchClauseContext ctx) {
        this.flowControllerMediatorSection.pop();
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTryCatchBlock(WUMLParser.TryCatchBlockContext ctx) {
        this.flowControllerStack.pop();
    }

    /* Util methods */
    /**
     * Use correct mediation flow to place the mediator when filter mediator is present
     *
     * @param mediator
     */
    private void dropMediatorFilterAware(Mediator mediator) {
        if (!flowControllerMediatorSection.empty()) {
            switch (flowControllerMediatorSection.peek()) {
            case ifBlock:
                ((FilterMediator) flowControllerStack.peek()).addThenMediator(mediator);
                break;
            case elseBlock:
                ((FilterMediator) flowControllerStack.peek()).addOtherwiseMediator(mediator);
                break;
            case tryBlock:
                ((TryBlockMediator) flowControllerStack.peek()).addThenMediator(mediator);
                break;
            case catchBlock:
                ((TryBlockMediator) flowControllerStack.peek()).peekExceptionHandlers().addChildMediator(mediator);
                break;
            }
        } else {
            this.currentResource.getDefaultWorker().addMediator(mediator);
        }
    }

    enum FlowControllerMediatorSection {
        ifBlock, elseBlock, tryBlock, catchBlock
    }

}
