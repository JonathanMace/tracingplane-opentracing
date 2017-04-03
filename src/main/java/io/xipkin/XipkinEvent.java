package io.xipkin;

public interface XipkinEvent {
    
    public XipkinEvent addParent(EventId parentEventId);
    
    public XipkinEvent annotate(Object key, Object value); 

}
