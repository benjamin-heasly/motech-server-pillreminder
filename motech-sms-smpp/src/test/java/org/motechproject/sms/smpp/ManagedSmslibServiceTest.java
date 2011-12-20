package org.motechproject.sms.smpp;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.gateway.OutboundEventGateway;
import org.motechproject.sms.smpp.constants.SmppProperties;
import org.motechproject.sms.smpp.constants.SmsProperties;
import org.smslib.*;
import org.smslib.smpp.jsmpp.JSMPPGateway;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class ManagedSmslibServiceTest {
	@Mock
	private Service smslibService;
	@Mock
	private OutboundEventGateway outboundEventGateway;

	private Properties smppProperties;
	private Properties smsProperties;

	@Before
	public void setup() {
		initMocks(this);

		smppProperties = new Properties() {{
			setProperty(SmppProperties.HOST, "smppserver.com");
			setProperty(SmppProperties.PASSWORD, "wpsd");
			setProperty(SmppProperties.PORT, "8876");
			setProperty(SmppProperties.SYSTEM_ID, "pavel");
		}};
		smsProperties = new Properties();
	}

	@Test
	public void shouldConnectOnApplicationStartup() throws NoSuchMethodException {
		Method connect = ManagedSmslibService.class.getDeclaredMethod("connect", new Class[]{});
		assertTrue("PostConstruct annotation missing", connect.isAnnotationPresent(PostConstruct.class));
	}

	@Test
	public void shouldDisconnectOnApplicationShutdown() throws NoSuchMethodException {
		Method disconnect = ManagedSmslibService.class.getDeclaredMethod("disconnect", new Class[]{});
		assertTrue("PreDestroy annotation missing", disconnect.isAnnotationPresent(PreDestroy.class));
	}

    @Test
    public void shouldRegisterOutboundNotificationListener() {
        OutboundMessageNotification outboundMessageNotification = mock(OutboundMessageNotification.class);
        new ManagedSmslibService(smslibService, smsProperties, smppProperties, outboundMessageNotification, null);
        verify(smslibService).setOutboundMessageNotification(outboundMessageNotification);
    }

    @Test
    public void shouldRegisterInboundMessagesListener() {
        InboundMessageNotification inboundMessageNotification = mock(InboundMessageNotification.class);
        new ManagedSmslibService(smslibService, smsProperties, smppProperties, null, inboundMessageNotification);
        verify(smslibService).setInboundMessageNotification(inboundMessageNotification);
    }

	@Test
	public void shouldAddConfiguredJsmppGatewayDuringInitialization() throws GatewayException {
		new ManagedSmslibService(smslibService, smsProperties, smppProperties, null, null);

		ArgumentCaptor<JSMPPGateway> jsmppGatewayCaptor = ArgumentCaptor.forClass(JSMPPGateway.class);
		verify(smslibService).addGateway(jsmppGatewayCaptor.capture());

		JSMPPGateway gateway = jsmppGatewayCaptor.getValue();
		assertEquals("smppserver.com", gateway.getHost());
		assertEquals(8876, gateway.getPort());
		assertEquals("pavel", gateway.getBindAttributes().getSystemId());
		assertEquals("wpsd", gateway.getBindAttributes().getPassword());
	}

	@Test
	public void shouldConfigureRetryCountOnSmsLib() {
		Service actualSmslibService = Service.getInstance();
		Properties smsProperties = new Properties() {{
			setProperty(SmsProperties.MAX_RETRIES, "5");
		}};
		new ManagedSmslibService(actualSmslibService, smsProperties, smppProperties, null, null);
		assertEquals(5, actualSmslibService.getSettings().QUEUE_RETRIES);
	}

	@Test
	public void shouldEstablishSmppConnection() throws SMSLibException, IOException, InterruptedException {
		ManagedSmslibService managedSmslibService = new ManagedSmslibService(smslibService, smsProperties, smppProperties, null, null);
		managedSmslibService.connect();
		verify(smslibService).startService();
	}

	@Test
	public void shouldTerminateSmppConnection() throws IOException, SMSLibException, InterruptedException {
		ManagedSmslibService managedSmslibService = new ManagedSmslibService(smslibService, smsProperties, smppProperties, null, null);
		managedSmslibService.disconnect();
		verify(smslibService).stopService();
	}

	@Test
	public void shouldSendSmsAsynchronously() throws GatewayException, IOException, TimeoutException, InterruptedException {
		ManagedSmslibService managedSmslibService = new ManagedSmslibService(smslibService, smsProperties, smppProperties, null, null);
		managedSmslibService.queueMessage(Arrays.asList("recipient1", "recipient2"), "message");

		ArgumentCaptor groupNameCaptor = ArgumentCaptor.forClass(String.class);
		verify(smslibService).createGroup((String) groupNameCaptor.capture());

		verify(smslibService).addToGroup((String) groupNameCaptor.getValue(), "recipient1");
		verify(smslibService).addToGroup((String) groupNameCaptor.getValue(), "recipient2");

		ArgumentCaptor<OutboundMessage> outboundMessageCaptor = ArgumentCaptor.forClass(OutboundMessage.class);
		verify(smslibService).queueMessage(outboundMessageCaptor.capture());

		assertEquals("message", outboundMessageCaptor.getValue().getText());
		assertEquals(groupNameCaptor.getValue(), outboundMessageCaptor.getValue().getRecipient());

		verify(smslibService).removeGroup((String) groupNameCaptor.getValue());
	}
}