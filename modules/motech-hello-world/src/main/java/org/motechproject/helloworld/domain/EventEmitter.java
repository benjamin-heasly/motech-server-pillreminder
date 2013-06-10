package org.motechproject.helloworld.domain;

public interface EventEmitter {

    void emitEvent();

    String getSubject();

}
