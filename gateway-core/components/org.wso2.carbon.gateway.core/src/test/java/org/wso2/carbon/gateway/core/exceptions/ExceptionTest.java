package org.wso2.carbon.gateway.core.exceptions;

import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.gateway.core.exception.ConnectionClosedException;
import org.wso2.carbon.gateway.core.exception.ConnectionTimeoutException;
import org.wso2.carbon.gateway.core.exception.ConnectionTimeoutExceptionHandler;
import org.wso2.carbon.gateway.core.exception.CustomExceptionHandler;
import org.wso2.carbon.gateway.core.exception.DefaultExceptionHandler;
import org.wso2.carbon.gateway.core.exception.FlowControllerExceptionCallback;
import org.wso2.carbon.gateway.core.flow.FlowControllerMediateCallback;
import org.wso2.carbon.gateway.core.flow.Mediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.TryBlockMediator;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Stack;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class tests exception handling implementation.
 */
public class ExceptionTest {

    @Test
    public void chainingMediationAndExceptionCallbackTest() {

        // Netty Callback
        CarbonCallback carbonCallback = Mockito.mock(CarbonCallback.class);
        Mediator mediator = Mockito.mock(Mediator.class);
        Stack variableStack = Mockito.mock(Stack.class);

        // CarbonMessage
        CarbonMessage faultyCarbonMessage = Mockito.mock(CarbonMessage.class);
        Mockito.when(faultyCarbonMessage.isFaulty()).thenReturn(true);
        Mockito.when(faultyCarbonMessage.getNelException())
                .thenReturn(Mockito.mock(ConnectionTimeoutException.class));

        // try block mediator that cannot handle the exception
        Mediator tryBlockMediatorCant = Mockito.mock(TryBlockMediator.class);
        Mockito.when(((TryBlockMediator) tryBlockMediatorCant).hasExceptionHandler())
                .thenReturn(true).thenReturn(false);
        Mockito.when(((TryBlockMediator) tryBlockMediatorCant).popHandler()).thenReturn(new CustomExceptionHandler());

        DefaultExceptionHandler defaultExceptionHandler = Mockito.mock(DefaultExceptionHandler.class);

        // chain with can't handle scenario
        CarbonCallback handleException1 = new FlowControllerExceptionCallback(carbonCallback,
                tryBlockMediatorCant, variableStack, defaultExceptionHandler);
        new FlowControllerMediateCallback(handleException1, mediator, variableStack).done(faultyCarbonMessage);

        Mockito.verify(defaultExceptionHandler, Mockito.times(1)).handleException(
                faultyCarbonMessage, carbonCallback);
    }

    @Test
    public void chainingTwoExceptionCallbackTest() {

        // Netty Callback
        CarbonCallback carbonCallback = Mockito.mock(CarbonCallback.class);
        Stack variableStack = Mockito.mock(Stack.class);

        // CarbonMessage
        CarbonMessage faultyCarbonMessage = Mockito.mock(CarbonMessage.class);
        Mockito.when(faultyCarbonMessage.isFaulty()).thenReturn(true);
        Mockito.when(faultyCarbonMessage.getNelException())
                .thenReturn(Mockito.mock(ConnectionTimeoutException.class));

        // try block mediator that cannot handle the exception
        Mediator tryBlockMediatorCant = Mockito.mock(TryBlockMediator.class);
        Mockito.when(((TryBlockMediator) tryBlockMediatorCant).hasExceptionHandler())
                .thenReturn(true).thenReturn(false);
        Mockito.when(((TryBlockMediator) tryBlockMediatorCant).popHandler()).thenReturn(new CustomExceptionHandler());

        // try block mediator that cannot handle the exception
        Mediator tryBlockMediatorCant1 = Mockito.mock(TryBlockMediator.class);
        Mockito.when(((TryBlockMediator) tryBlockMediatorCant1).hasExceptionHandler())
                .thenReturn(true).thenReturn(false);
        Mockito.when(((TryBlockMediator) tryBlockMediatorCant1).popHandler()).thenReturn(new CustomExceptionHandler());

        DefaultExceptionHandler defaultExceptionHandler = Mockito.mock(DefaultExceptionHandler.class);

        // chain with can't handle scenario
        CarbonCallback handleException1 = new FlowControllerExceptionCallback(carbonCallback,
                tryBlockMediatorCant1, variableStack, defaultExceptionHandler);
        new FlowControllerExceptionCallback(handleException1, tryBlockMediatorCant, variableStack,
                Mockito.mock(DefaultExceptionHandler.class)).done(faultyCarbonMessage);

        Mockito.verify(defaultExceptionHandler, Mockito.times(1)).handleException(
                faultyCarbonMessage, carbonCallback);
    }

    @Test
    public void tryBlockTest() {
        CarbonCallback carbonCallback = Mockito.mock(CarbonCallback.class);
        Stack variableStack = Mockito.mock(Stack.class);
        ConnectionTimeoutException connectionTimeoutException = Mockito.mock(ConnectionTimeoutException.class);

        // CarbonMessage
        CarbonMessage faultyCarbonMessage = Mockito.mock(CarbonMessage.class);
        Mockito.when(faultyCarbonMessage.isFaulty()).thenReturn(true);
        Mockito.when(faultyCarbonMessage.getNelException()).thenReturn(connectionTimeoutException);

        ConnectionTimeoutExceptionHandler conectionTimeoutExceptionHandler = Mockito.mock(
                ConnectionTimeoutExceptionHandler.class);
        Mockito.when(conectionTimeoutExceptionHandler.canHandle(connectionTimeoutException)).thenReturn(true);

        // try block mediator that cannot handle the exception
        Mediator tryBlockMediatorCan = Mockito.mock(TryBlockMediator.class);
        Mockito.when(((TryBlockMediator) tryBlockMediatorCan).hasExceptionHandler())
                .thenReturn(true).thenReturn(false);
        Mockito.when(((TryBlockMediator) tryBlockMediatorCan).popHandler())
                .thenReturn(conectionTimeoutExceptionHandler);

        DefaultExceptionHandler defaultExceptionHandler = Mockito.mock(DefaultExceptionHandler.class);

        new FlowControllerExceptionCallback(carbonCallback,
                tryBlockMediatorCan, variableStack, defaultExceptionHandler).done(faultyCarbonMessage);

        Mockito.verify(conectionTimeoutExceptionHandler, Mockito.times(1))
                .handleException(faultyCarbonMessage, carbonCallback);
    }

    @Test
    public void connectionTimeoutExceptionHandlerTest() {
        ConnectionTimeoutException connectionTimeoutException = Mockito.mock(ConnectionTimeoutException.class);
        ConnectionTimeoutExceptionHandler exHandler = new ConnectionTimeoutExceptionHandler();
        assertTrue(exHandler.canHandle(connectionTimeoutException));

        ConnectionClosedException connectionClosedException = Mockito.mock(ConnectionClosedException.class);
        assertFalse(exHandler.canHandle(connectionClosedException));
    }

//    @Test
//    public void chainOfResponsibilityCanHandleTest() {
//
//        // Netty Callback
//        CarbonCallback carbonCallback = Mockito.mock(CarbonCallback.class);
//        Mediator mediator = Mockito.mock(Mediator.class);
//        Stack variableStack = Mockito.mock(Stack.class);
//
//        // CarbonMessage
//        CarbonMessage faultyCarbonMessage = Mockito.mock(CarbonMessage.class);
//        Mockito.when(faultyCarbonMessage.isFaulty()).thenReturn(true);
//        Mockito.when(faultyCarbonMessage.getNelException())
//                .thenReturn(Mockito.mock(ConnectionTimeoutException.class));
//
//        // try block mediator that cannot handle the exception
//        Mediator tryBlockMediatorCant = Mockito.mock(TryBlockMediator.class);
//        Mockito.when(((TryBlockMediator) tryBlockMediatorCant).hasExceptionHandler())
//                .thenReturn(true).thenReturn(false);
//        Mockito.when(((TryBlockMediator) tryBlockMediatorCant).popHandler()).thenReturn(new CustomExceptionHandler());
//
//        // try block mediator that can handle the exception
//        Mediator tryBlockCan = Mockito.mock(TryBlockMediator.class);
//        Mockito.when(((TryBlockMediator) tryBlockCan).hasExceptionHandler()).thenReturn(true).thenReturn(false);
//        Mockito.when(((TryBlockMediator) tryBlockCan).popHandler())
//            .thenReturn(new ConnectionTimeoutExceptionHandler());
//
//        DefaultExceptionHandler defaultExceptionHandler = Mockito.mock(DefaultExceptionHandler.class);
//
//        // chain with can handle scenario
//        CarbonCallback handleException2 = new FlowControllerExceptionCallback(carbonCallback,
//                tryBlockCan, variableStack, defaultExceptionHandler);
//        CarbonCallback handleException1 = new FlowControllerExceptionCallback(handleException2,
//                tryBlockMediatorCant, variableStack, defaultExceptionHandler);
//        CarbonCallback mediateCallBack = new FlowControllerMediateCallback(handleException1, mediator, variableStack);
//
//        mediateCallBack.done(faultyCarbonMessage);
//    }
}
