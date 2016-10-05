package org.wso2.carbon.gateway.core.exceptions;

import org.junit.Test;
import org.wso2.carbon.gateway.core.config.ParameterHolder;
import org.wso2.carbon.gateway.core.exception.ConnectionClosedException;
import org.wso2.carbon.gateway.core.exception.ConnectionTimeoutException;
import org.wso2.carbon.gateway.core.exception.ConnectionTimeoutExceptionHandler;
import org.wso2.carbon.gateway.core.exception.CustomExceptionHandler;
import org.wso2.carbon.gateway.core.exception.DefaultExceptionHandler;
import org.wso2.carbon.gateway.core.exception.FlowControllerExceptionCallback;
import org.wso2.carbon.gateway.core.flow.FlowControllerMediateCallback;
import org.wso2.carbon.gateway.core.flow.Mediator;
import org.wso2.carbon.gateway.core.flow.Resource;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.flowcontrollers.filter.TryBlockMediator;
import org.wso2.carbon.gateway.core.flow.mediators.builtin.manipulators.log.LogMediator;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Stack;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This class tests exception handling implementation.
 */
public class ExceptionTest {

    @Test
    public void chainingMediationAndExceptionCallbackTest() {

        // Netty Callback
        CarbonCallback carbonCallback = mock(CarbonCallback.class);
        Mediator mediator = mock(Mediator.class);
        Stack variableStack = mock(Stack.class);

        // CarbonMessage
        CarbonMessage faultyCarbonMessage = mock(CarbonMessage.class);
        when(faultyCarbonMessage.isFaulty()).thenReturn(true);
        when(faultyCarbonMessage.getNelException()).thenReturn(mock(ConnectionTimeoutException.class));

        // try block mediator that cannot handle the exception
        Mediator tryBlockMediatorCannot = mock(TryBlockMediator.class);
        when(((TryBlockMediator) tryBlockMediatorCannot).hasExceptionHandler()).thenReturn(true).thenReturn(false);
        when(((TryBlockMediator) tryBlockMediatorCannot).popHandler()).thenReturn(new CustomExceptionHandler());

        DefaultExceptionHandler defaultExceptionHandler = mock(DefaultExceptionHandler.class);

        // chain with can't handle scenario
        CarbonCallback handleException = new FlowControllerExceptionCallback(carbonCallback,
                tryBlockMediatorCannot, variableStack, defaultExceptionHandler);
        new FlowControllerMediateCallback(handleException, mediator, variableStack).done(faultyCarbonMessage);

        verify(defaultExceptionHandler, times(1)).handleException(faultyCarbonMessage, carbonCallback);
    }

    @Test
    public void chainingTwoExceptionCallbackTest() {

        // Netty Callback
        CarbonCallback carbonCallback = mock(CarbonCallback.class);
        Stack variableStack = mock(Stack.class);

        // CarbonMessage
        CarbonMessage faultyCarbonMessage = mock(CarbonMessage.class);
        when(faultyCarbonMessage.isFaulty()).thenReturn(true);
        when(faultyCarbonMessage.getNelException()).thenReturn(mock(ConnectionTimeoutException.class));

        // try block mediator that cannot handle the exception
        Mediator tryBlockMediatorCannot = mock(TryBlockMediator.class);
        when(((TryBlockMediator) tryBlockMediatorCannot).hasExceptionHandler()).thenReturn(true).thenReturn(false);
        when(((TryBlockMediator) tryBlockMediatorCannot).popHandler()).thenReturn(new CustomExceptionHandler());

        // try block mediator that cannot handle the exception
        Mediator tryBlockMediatorCannot1 = mock(TryBlockMediator.class);
        when(((TryBlockMediator) tryBlockMediatorCannot1).hasExceptionHandler()).thenReturn(true).thenReturn(false);
        when(((TryBlockMediator) tryBlockMediatorCannot1).popHandler()).thenReturn(new CustomExceptionHandler());

        DefaultExceptionHandler defaultExceptionHandler = mock(DefaultExceptionHandler.class);

        // chain with can't handle scenario
        CarbonCallback handleException1 = new FlowControllerExceptionCallback(carbonCallback,
                tryBlockMediatorCannot1, variableStack, defaultExceptionHandler);
        new FlowControllerExceptionCallback(handleException1, tryBlockMediatorCannot, variableStack,
                mock(DefaultExceptionHandler.class)).done(faultyCarbonMessage);

        verify(defaultExceptionHandler, times(1)).handleException(faultyCarbonMessage, carbonCallback);
    }

    @Test
    public void tryBlockTest() {
        CarbonCallback carbonCallback = mock(CarbonCallback.class);
        Stack variableStack = mock(Stack.class);
        ConnectionTimeoutException connectionTimeoutException = mock(ConnectionTimeoutException.class);

        // CarbonMessage
        CarbonMessage faultyCarbonMessage = mock(CarbonMessage.class);
        when(faultyCarbonMessage.isFaulty()).thenReturn(true);
        when(faultyCarbonMessage.getNelException()).thenReturn(connectionTimeoutException);

        ConnectionTimeoutExceptionHandler conectionTimeoutExceptionHandler = mock(
                ConnectionTimeoutExceptionHandler.class);
        when(conectionTimeoutExceptionHandler.canHandle(connectionTimeoutException)).thenReturn(true);

        // try block mediator that cannot handle the exception
        Mediator tryBlockMediatorCan = mock(TryBlockMediator.class);
        when(((TryBlockMediator) tryBlockMediatorCan).hasExceptionHandler()).thenReturn(true).thenReturn(false);
        when(((TryBlockMediator) tryBlockMediatorCan).popHandler()).thenReturn(conectionTimeoutExceptionHandler);

        DefaultExceptionHandler defaultExceptionHandler = mock(DefaultExceptionHandler.class);

        new FlowControllerExceptionCallback(carbonCallback,
                tryBlockMediatorCan, variableStack, defaultExceptionHandler).done(faultyCarbonMessage);

        verify(conectionTimeoutExceptionHandler, times(1)).handleException(faultyCarbonMessage, carbonCallback);
    }

    @Test
    public void connectionTimeoutExceptionHandlerTest() {
        ConnectionTimeoutException connectionTimeoutException = mock(ConnectionTimeoutException.class);
        ConnectionTimeoutExceptionHandler exHandler = new ConnectionTimeoutExceptionHandler();
        assertTrue(exHandler.canHandle(connectionTimeoutException));

        ConnectionClosedException connectionClosedException = mock(ConnectionClosedException.class);
        assertFalse(exHandler.canHandle(connectionClosedException));
    }

    @Test
    public void mediatorAfterTryBlockMediatorTest() throws Exception {
        Resource resource = new Resource("TestResource");

        // Mocking a faulty carbon message
        CarbonMessage carbonMessage = mock(CarbonMessage.class);
        when(carbonMessage.isFaulty()).thenReturn(true);
        when(carbonMessage.getNelException()).thenReturn(mock(ConnectionTimeoutException.class));

        CarbonCallback carbonCallback = mock(CarbonCallback.class);

        MockMediator triggerException = new MockMediator();
        LogMediator logInsideCatch =  mock(LogMediator.class);
        LogMediator outSideCatch = mock(LogMediator.class);

        TryBlockMediator tryBlockMediator = new TryBlockMediator();
        tryBlockMediator.addThenMediator(triggerException);

        ConnectionTimeoutExceptionHandler timeoutHandler = new ConnectionTimeoutExceptionHandler();
        timeoutHandler.addChildMediator(logInsideCatch);
        tryBlockMediator.addHandler(timeoutHandler);

        resource.getDefaultWorker().addMediator(tryBlockMediator);
        resource.getDefaultWorker().addMediator(outSideCatch);

        resource.receive(carbonMessage, carbonCallback);
        verify(outSideCatch, times(1)).receive(carbonMessage, carbonCallback);
    }

    /**
     * This mock mediator is use to trigger error sequence.
     */
    private class MockMediator implements Mediator {
        @Override
        public String getName() {
            return null;
        }

        @Override
        public void setNext(Mediator nextMediator) {}

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public boolean next(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
            return false;
        }

        @Override
        public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
            carbonCallback.done(carbonMessage);
            return true;
        }

        @Override
        public void setParameters(ParameterHolder parameters) {}

        @Override
        public Object getValue(CarbonMessage carbonMessage, String name) {
            return null;
        }
    }
}
