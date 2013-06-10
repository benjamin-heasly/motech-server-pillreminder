package org.motechproject.helloworld.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;

import org.motechproject.helloworld.domain.EventEmitter;

@Controller
public class EventEmitterController {
    
    @Autowired
    private EventEmitter eventEmitter;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/event-emitter")
    @ResponseBody
    public String emitEvent() {
        eventEmitter.emitEvent();
        String message = "Emitted an event with subject " + eventEmitter.getSubject();
        logger.info(message);
        return message;
    }
}
