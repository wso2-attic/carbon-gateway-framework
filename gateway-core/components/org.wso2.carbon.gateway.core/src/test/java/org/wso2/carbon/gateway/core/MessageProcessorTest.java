package org.wso2.carbon.gateway.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.gateway.core.inbound.Dispatcher;
import org.wso2.carbon.gateway.core.inbound.InboundEPProviderRegistry;
import org.wso2.carbon.gateway.core.inbound.Provider;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import static org.mockito.Mockito.times;

/**
 * This test-case test the functionality of
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(InboundEPProviderRegistry.class)
public class MessageProcessorTest {

    @Test
    public void receiveTest() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        CarbonCallback carbonCallback = Mockito.mock(CarbonCallback.class);

        Provider provider = Mockito.mock(Provider.class);
        Dispatcher dispatcher = Mockito.mock(Dispatcher.class);

        InboundEPProviderRegistry inboundEPProviderRegistry = Mockito.mock(InboundEPProviderRegistry.class);
        PowerMockito.mockStatic(InboundEPProviderRegistry.class);

        Mockito.when(InboundEPProviderRegistry.getInstance()).thenReturn(inboundEPProviderRegistry);
        Mockito.when(inboundEPProviderRegistry.getProvider("http")).thenReturn(provider);
        Mockito.when(provider.getInboundEndpointDispatcher()).thenReturn(dispatcher);

        MessageProcessor messageProcessor = new MessageProcessor();
        messageProcessor.receive(carbonMessage, carbonCallback);

        Mockito.verify(dispatcher, times(1)).dispatch(carbonMessage, carbonCallback);
    }
}
