package io.xipkin.impl;

import io.xipkin.EventId;
import io.xipkin.XipkinEvent;

public class NoopXipkinEvent implements XipkinEvent {
    
    public static final NoopXipkinEvent instance = new NoopXipkinEvent();
    
    private NoopXipkinEvent() {}

    @Override
    public XipkinEvent annotate(Object key, Object value) {
        return this;
    }

    @Override
    public XipkinEvent addParent(EventId parentEventId) {
        return this;
    }

}
