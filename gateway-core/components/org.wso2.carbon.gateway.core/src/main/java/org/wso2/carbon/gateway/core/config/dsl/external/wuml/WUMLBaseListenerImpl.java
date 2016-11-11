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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
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
    // Used when mediator returns a value
    private String nextMediatorReturnParameter;
    // Handling statements like "message n = invoke(endpoint,m)". Here we will add a property and a call mediator
    private boolean isInitializationFired;
    private Mediator initializerMediator;

    public WUMLBaseListenerImpl(String fileName) {
        this.integrationName = fileName;
        this.flowControllerStack = new Stack<>();
        this.flowControllerMediatorSection = new Stack<>();
        this.nextMediatorReturnParameter = null;
        this.isInitializationFired = false;
        this.initializerMediator = null;
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
        InboundEndpoint inboundEndpoint;
        ParameterHolder parameterHolder = new ParameterHolder();
        Map<String, String> valueMap = new HashMap<>();
        String protocol;

        if (ctx.sourceElementValuePairs().interfaceDeclaration() != null) {
            String interfaceName = StringParserUtil.getValueWithinDoubleQuotes(
                    ctx.sourceElementValuePairs().interfaceDeclaration().StringLiteral().getText());
            protocol = "http";
            valueMap.put("interface", interfaceName);
            parameterHolder.addParameter(new Parameter("interface", interfaceName));
        } else {
            String host = StringParserUtil
                    .getValueWithinDoubleQuotes(ctx.sourceElementValuePairs().host().StringLiteral().getText());
            String port = ctx.sourceElementValuePairs().port().IntegerLiteral().getText();
            protocol = StringParserUtil
                    .getValueWithinDoubleQuotes(ctx.sourceElementValuePairs().protocol().StringLiteral().getText());

            valueMap.put(Constants.HOST, host);
            valueMap.put(Constants.PORT, port);

            parameterHolder.addParameter(new Parameter(Constants.HOST, host));
            parameterHolder.addParameter(new Parameter(Constants.PORT, port));
        }

        valueMap.put(Constants.PROTOCOL, protocol);
        integration.getAnnotation(ConfigConstants.AN_SOURCE).setValue(valueMap);
        parameterHolder.addParameter(new Parameter(Constants.CONTEXT,
                integration.getAnnotation(ConfigConstants.AN_BASE_PATH).getValue().toString()));

        inboundEndpoint = InboundEPProviderRegistry.getInstance().getProvider(protocol).getInboundEndpoint();
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
            //String endpointType = ctx.getChild(5).getText();
            String uriAsString = StringParserUtil.getValueWithinDoubleQuotes(ctx.StringLiteral().getText());
            URI endpoint = null;
            try {
                endpoint = new URI(uriAsString);
            } catch (URISyntaxException ex) {
                //endpoint uri syntax error
                log.error("Endpoint syntax error occurred. Failed to add outbound endpoint to configuration.", ex);
                return;
            }
            //since protocol type is redundant in endpoint URI
            OutboundEndpoint outboundEndpoint = OutboundEPProviderRegistry.getInstance()
                    .getProvider(endpoint.getScheme()).getEndpoint();
            outboundEndpoint.setName(ctx.Identifier().get(0).getText());
            outboundEndpoint.setUri(uriAsString);
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
        currentResource.setInputParamIdentifier(ctx.Identifier().getText());
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
    public void exitResourceName(WUMLParser.ResourceNameContext ctx) {
        /* Creating the resource */
        this.currentResource = new Resource(ctx.Identifier().getText());
    }

    /**
     * Handle events regarding all the data manipulation mediators (including custom mediators)
     * @param ctx
     */
    @Override
    public void exitMediatorCall(WUMLParser.MediatorCallContext ctx) {
        String mediatorId =  ctx.Identifier().getText();
        // call mediator is specified in the language as "invoke". This is a special case.
        if (Constants.INVOKE_STATEMENT.equals(mediatorId)) {
            mediatorId = Constants.CALL_MEDIATOR_NAME;
        } else if (Constants.HEADER_MEDIATOR_STATEMENT.equals(mediatorId)) {
            mediatorId = Constants.HEADER_MEDIATOR_NAME;
        }

        Mediator mediator = MediatorProviderRegistry.getInstance().getMediator(mediatorId);

        if (mediator != null) {
            ParameterHolder parameterHolder  = new ParameterHolder();

            // passing all the mediator input arguments
            processKeyValuePairs(ctx.keyValuePairs(), parameterHolder);

            //Adding the return variable key
            if (ctx.parent instanceof WUMLParser.LocalVariableInitializationStatementContext) {
                parameterHolder.addParameter(new Parameter(Constants.RETURN_VALUE,
                        ((WUMLParser.LocalVariableInitializationStatementContext)
                                ((WUMLParser.MediatorCallContext) ctx).parent).Identifier().getText()));
            } else if (ctx.parent instanceof WUMLParser.LocalVariableAssignmentStatementContext) {
                parameterHolder.addParameter(new Parameter(Constants.RETURN_VALUE,
                        ((WUMLParser.LocalVariableAssignmentStatementContext)
                                ((WUMLParser.MediatorCallContext) ctx).parent).Identifier().getText()));
            }

            // setting the integration name to the mediator. It can use useful in writing the operation in the mediator
            parameterHolder.addParameter(new Parameter(Constants.INTEGRATION_KEY, this.integrationName));

            mediator.setParameters(parameterHolder);
            dropMediatorFilterAware(mediator);
        }  else {
            log.warn("Mediator with the name : " + mediatorId + " is not found.");
        }
    }

    /**
     * statements with 'reply'. It maps to a Respond mediator
     */
    @Override
    public void exitReturnStatement(WUMLParser.ReturnStatementContext ctx) {
        Mediator respondMediator = MediatorProviderRegistry.getInstance().getMediator(Constants.RESPOND_MEDIATOR_NAME);
        ParameterHolder parameterHolder = new ParameterHolder();
        if (ctx.Identifier() != null) {
            String messageId = ctx.Identifier().getText();
            parameterHolder.addParameter(new Parameter("messageId", messageId));
        }

        respondMediator.setParameters(parameterHolder);
        dropMediatorFilterAware(respondMediator);
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
            if (expressions != null && expressions.size() == 2) {
                // if the format is : "eval("$p1.p2")=="abc""
                if (expressions.get(0).evalExpression() != null && expressions.get(1).literal() != null) {
                    // if the messageRef is given, extract the message variable name
                    String messageIdentifier = null;
                    if (expressions.get(0).evalExpression().Identifier().size() == 2 && Constants.MESSAGE_KEY
                            .equals(expressions.get(0).evalExpression().Identifier().get(0).getText())) {
                        messageIdentifier = expressions.get(0).evalExpression().Identifier().get(1).getText();
                    } else {
                        log.error("messageRef value is not set in the eval expression.");
                    }

                    String sourceDefinition;
                    Source source;
                    if (expressions.get(0).evalExpression().StringLiteral() != null) {
                        sourceDefinition = StringParserUtil.getValueWithinDoubleQuotes(expressions.get(0)
                                .evalExpression().StringLiteral().getText());
                        source = new Source(sourceDefinition);
                    } else {
                        String pathLanguage = expressions.get(0).evalExpression().pathExpression().Identifier()
                                .getText();
                        sourceDefinition = StringParserUtil.getValueWithinDoubleQuotes(
                                expressions.get(0).evalExpression().pathExpression().StringLiteral().getText());
                        source = new Source(sourceDefinition, pathLanguage);
                    }
                    String conditionValue = StringParserUtil
                            .getValueWithinDoubleQuotes(expressions.get(1).literal().StringLiteral().getText());

                    Condition condition = new Condition(source, Pattern.compile(conditionValue));

                    FilterMediator filterMediator = new FilterMediator(condition, messageIdentifier);
                    dropMediatorFilterAware(filterMediator);
                    flowControllerStack.push(filterMediator);
                    this.flowControllerMediatorSection.push(FlowControllerMediatorSection.ifBlock);
                } else {
                    //TODO: Support other types of expressions. eg: ("aa"=="bb"), ("bb"==eval(...))
                    log.error("Unsupported expression: " + ctx.getText());
                }
            }
        }
    }

    @Override
    public void exitIfElseBlock(WUMLParser.IfElseBlockContext ctx) {
        if (!this.flowControllerStack.empty()) {
            this.flowControllerStack.pop();
        }
    }

    @Override
    public void enterIfBlock(WUMLParser.IfBlockContext ctx) {
    }

    @Override
    public void exitIfBlock(WUMLParser.IfBlockContext ctx) {
        if (!this.flowControllerMediatorSection.empty()) {
            this.flowControllerMediatorSection.pop();
        }
    }

    @Override
    public void enterElseBlock(WUMLParser.ElseBlockContext ctx) {
        this.flowControllerMediatorSection.push(FlowControllerMediatorSection.elseBlock);
    }

    @Override
    public void exitElseBlock(WUMLParser.ElseBlockContext ctx) {
        if (!this.flowControllerMediatorSection.empty()) {
            this.flowControllerMediatorSection.pop();
        }
    }


    /* Try-catch parsing */

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterTryClause(WUMLParser.TryClauseContext ctx) {
        TryBlockMediator tryBlockMediator = new TryBlockMediator();
        dropMediatorFilterAware(tryBlockMediator);
        flowControllerStack.push(tryBlockMediator);
        this.flowControllerMediatorSection.push(FlowControllerMediatorSection.tryBlock);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitTryClause(WUMLParser.TryClauseContext ctx) {
        if (!this.flowControllerMediatorSection.empty()) {
            this.flowControllerMediatorSection.pop();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitExceptionHandler(WUMLParser.ExceptionHandlerContext ctx) {

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
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitCatchClause(WUMLParser.CatchClauseContext ctx) {
        if (!this.flowControllerMediatorSection.empty()) {
            this.flowControllerMediatorSection.pop();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void exitTryCatchBlock(WUMLParser.TryCatchBlockContext ctx) {
        if (!this.flowControllerStack.empty()) {
            this.flowControllerStack.pop();
        }
    }

    /* Variable Handling */

    @Override
    public void exitLocalVariableDeclarationStatement(WUMLParser.LocalVariableDeclarationStatementContext ctx) {
        String type = null;
        String variableName;
        ParameterHolder parameterHolder = new ParameterHolder();
        Mediator propertyMediator = MediatorProviderRegistry.getInstance()
                .getMediator(Constants.PROPERTY_MEDIATOR_NAME);

        if (ctx.type() != null) { // pattern of "type Identifier ';'"
            type = ctx.type().getText();
        } else if (ctx.classType() != null) { // pattern of "message m ';'"
            type = ctx.classType().getText();
        }

        variableName = ctx.Identifier().getText();
        parameterHolder.addParameter(new Parameter(Constants.KEY, variableName));
        parameterHolder.addParameter(new Parameter(Constants.TYPE, type));
        parameterHolder.addParameter(new Parameter(Constants.ASSIGNMENT, Boolean.FALSE.toString()));

        propertyMediator.setParameters(parameterHolder);
        dropMediatorFilterAware(propertyMediator);
    }

    @Override
    public void enterLocalVariableInitializationStatement(WUMLParser.LocalVariableInitializationStatementContext ctx) {
        isInitializationFired = true;
    }

    @Override
    public void exitLocalVariableInitializationStatement(WUMLParser.LocalVariableInitializationStatementContext ctx) {
        String type = null;
        String variableName = null;
        String variableValue;
        ParameterHolder parameterHolder = new ParameterHolder();
        Mediator propertyMediator = MediatorProviderRegistry.getInstance()
                .getMediator(Constants.PROPERTY_MEDIATOR_NAME);

        if (ctx.type() != null) { // pattern of " type Identifier '=' literal ';' "
            type = ctx.type().getText();
            variableName = ctx.Identifier().getText();
            variableValue = (ctx.literal().StringLiteral() != null) ?
                    StringParserUtil.getValueWithinDoubleQuotes(ctx.literal().getText()) :
                    ctx.literal().getText();
            parameterHolder.addParameter(new Parameter(Constants.VALUE, variableValue));
        } else if (ctx.mediatorCall() != null) { // pattern of " message n = invoke(Ep,m) ';'"
            type = ctx.classType().getText();
            variableName = ctx.Identifier().getText();
        } else if (ctx.classType() != null && ctx.newTypeObjectCreation() != null) {
            type = ctx.classType().getText();   // pattern of " message m '=' new message() ';' "
            variableName = ctx.Identifier().getText();
        }

        parameterHolder.addParameter(new Parameter(Constants.KEY, variableName));
        parameterHolder.addParameter(new Parameter(Constants.TYPE, type));
        parameterHolder.addParameter(new Parameter(Constants.ASSIGNMENT, Boolean.FALSE.toString()));

        isInitializationFired = false;
        propertyMediator.setParameters(parameterHolder);
        dropMediatorFilterAware(propertyMediator);
        if (initializerMediator != null) {
            dropMediatorFilterAware(initializerMediator);
            initializerMediator = null;
        }

    }

    @Override
    public void exitLocalVariableAssignmentStatement(WUMLParser.LocalVariableAssignmentStatementContext ctx) {
        String type;
        String variableName;
        String variableValue;
        ParameterHolder parameterHolder = new ParameterHolder();
        Mediator propertyMediator = MediatorProviderRegistry.getInstance()
                .getMediator(Constants.PROPERTY_MEDIATOR_NAME);

        if (ctx.newTypeObjectCreation() != null) { // pattern of " m '=' new message() ';' "
            type = ctx.newTypeObjectCreation().classType().getText();
            variableName = ctx.Identifier().getText();
            parameterHolder.addParameter(new Parameter(Constants.TYPE, type));
            parameterHolder.addParameter(new Parameter(Constants.ASSIGNMENT, Boolean.FALSE.toString()));
        } else if (ctx.mediatorCall() != null) {
            return;
        } else {  // pattern of " i = 4 ';'"
            variableName = ctx.Identifier().getText();
            variableValue = (ctx.literal().StringLiteral() != null) ?
                    StringParserUtil.getValueWithinDoubleQuotes(ctx.literal().getText()) :
                    ctx.literal().getText();
            parameterHolder.addParameter(new Parameter(Constants.VALUE, variableValue));
            parameterHolder.addParameter(new Parameter(Constants.ASSIGNMENT, Boolean.TRUE.toString()));
        }
        parameterHolder.addParameter(new Parameter(Constants.KEY, variableName));

        propertyMediator.setParameters(parameterHolder);
        dropMediatorFilterAware(propertyMediator);
    }

    /* Util methods */

    /**
     * Use correct mediation flow to place the mediator when filter mediator is present
     *
     * @param mediator
     */
    private void dropMediatorFilterAware(Mediator mediator) {
        if (isInitializationFired) {
            initializerMediator = mediator;
        } else if (!flowControllerMediatorSection.empty() && !flowControllerStack.empty()) {
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

    /**
     * Read all key-value pairs and put those into the given ParameterHolder
     *
     * @param keyValuePairs KeyValuePairs object
     * @param parameterHolder ParameterHolder object that key-value pairs should be stored
     */
    private void processKeyValuePairs(WUMLParser.KeyValuePairsContext keyValuePairs,
            ParameterHolder parameterHolder) {
        String key, value;
        if (keyValuePairs != null) {
            for (WUMLParser.KeyValuePairContext keyValuePair : keyValuePairs.keyValuePair()) {
                if (keyValuePair.literal() != null) {
                    if (keyValuePair.literal().StringLiteral() != null) {
                        value = StringParserUtil
                                .getValueWithinDoubleQuotes(keyValuePair.literal().StringLiteral().getText());
                    } else {
                        value = keyValuePair.literal().getText();
                    }
                } else {
                    value = keyValuePair.Identifier(keyValuePair.Identifier().size() - 1).getText();
                }
                // if the key is a classType (eg: 'endpoint' or 'message')
                if (keyValuePair.classType() != null) {
                    key = keyValuePair.classType().getText();
                } else {
                    key = keyValuePair.Identifier(0).getText();
                }
                parameterHolder.addParameter(new Parameter(key, value));
            }
        }
    }

    private enum FlowControllerMediatorSection {
        ifBlock, elseBlock, tryBlock, catchBlock
    }

}
