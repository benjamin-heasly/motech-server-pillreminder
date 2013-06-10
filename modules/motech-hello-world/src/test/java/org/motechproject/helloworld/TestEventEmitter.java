package org.motechproject.helloworld;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.helloworld.domain.EventEmitter;
import org.motechproject.helloworld.domain.EventEmitterImpl;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestEventEmitter {

	@Mock
	private EventRelay eventRelay;

	private EventEmitter eventEmitter;

	@Before
	public void setUp() {
		initMocks(this);
		this.eventEmitter = new EventEmitterImpl(eventRelay);
	}

	@Test
	public void testEmitterNotNull() {
		assertNotNull(eventEmitter);
	}

	@Test
	public void emittedEvent() {
		ArgumentCaptor<MotechEvent> motechEventCaptor = ArgumentCaptor
				.forClass(MotechEvent.class);
		eventEmitter.emitEvent();
		verify(eventRelay).sendEventMessage(motechEventCaptor.capture());
		MotechEvent motechEvent = motechEventCaptor.getValue();
		Assert.assertEquals(motechEvent.getSubject(), eventEmitter.getSubject());
	}
}
