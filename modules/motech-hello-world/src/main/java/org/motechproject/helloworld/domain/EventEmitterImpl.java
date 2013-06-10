package org.motechproject.helloworld.domain;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

import org.motechproject.helloworld.EventKeys;

@Component
public class EventEmitterImpl implements EventEmitter {

    private static final String SUBJECT = EventKeys.HELLO_SUBJECT;

    @Autowired
    private EventRelay eventRelay;

    public EventEmitterImpl() {
    }

    public EventEmitterImpl(EventRelay eventRelay) {
        this.eventRelay = eventRelay;
    }

    @Override
    public void emitEvent() {
        Map<String, Object> helloEventParams = new HashMap<>();
        helloEventParams.put("hello", "world");
        MotechEvent motechEvent = new MotechEvent(SUBJECT, helloEventParams);
        eventRelay.sendEventMessage(motechEvent);
    }

    @Override
    public String getSubject() {
        return SUBJECT;
    }
}
