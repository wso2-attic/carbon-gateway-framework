package org.wso2.carbon.gateway.core;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.gateway.core.flow.MediatorType;
import org.wso2.carbon.gateway.core.inbound.InboundEPProviderRegistry;
import org.wso2.carbon.gateway.core.inbound.Provider;
import org.wso2.carbon.gateway.core.worker.WorkerModelDispatcher;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import static org.mockito.Mockito.times;

/**
 * This test-case test the functionality of MessageProcessor implementation
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(InboundEPProviderRegistry.class)
public class MessageProcessorTest {

   // @Test
    public void receiveTest() throws Exception {
        // Mock all the required objects
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        CarbonCallback carbonCallback = Mockito.mock(CarbonCallback.class);

        Provider provider = Mockito.mock(Provider.class);

        WorkerModelDispatcher workerModelDispatcher = Mockito.mock(WorkerModelDispatcher.class);
        PowerMockito.mockStatic(WorkerModelDispatcher.class);
//        InboundEPProviderRegistry inboundEPProviderRegistry = Mockito.mock(InboundEPProviderRegistry.class);
//        PowerMockito.mockStatic(InboundEPProviderRegistry.class);
//
//        Mockito.when(InboundEPProviderRegistry.getInstance()).thenReturn(inboundEPProviderRegistry);
//        Mockito.when(inboundEPProviderRegistry.getProvider("http")).thenReturn(provider);
//        Mockito.when(provider.getInboundEndpointDispatcher()).thenReturn(dispatcher);

        Mockito.when(WorkerModelDispatcher.getInstance()).thenReturn(workerModelDispatcher);

        MessageProcessor messageProcessor = new MessageProcessor();
        messageProcessor.receive(carbonMessage, carbonCallback);

        // Assert whether receive method invokes the dispatcher at the end
        Mockito.verify(workerModelDispatcher, times(1)).dispatch(carbonMessage, carbonCallback, MediatorType.CPU_BOUND);
    }
}
