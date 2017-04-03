package io.xipkin.impl;

import java.util.HashSet;
import java.util.Set;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.xipkin.EventId;
import io.xipkin.XipkinContext;
import io.xipkin.XipkinEvent;
import io.xipkin.XipkinUtils;

public class XipkinEventImpl implements XipkinEvent {

    // Events require a minimum of eventID and parent IDs
    public final EventId eventId;
    public final Set<EventId> parentEventIds = new HashSet<>();
    
    // Custom annotations added to the event, typically a combination of user-supplied message and tracer-supplied timestamps
    public final Multimap<Object, Object> annotations = HashMultimap.create();

    private XipkinEventImpl(EventId eventId, Set<EventId> parentEventIds) {
        this.eventId = eventId;
        this.parentEventIds.addAll(parentEventIds);
    }

    public static XipkinEventImpl generate() {
        XipkinContext x = XipkinContext.get();
        if (x == null || x.parentEventIds == null || x.parentEventIds.isEmpty()) {
            return null;
        } else {
            long traceId = x.parentEventIds.iterator().next().traceId;
            EventId eventId = XipkinUtils.nextSpanId(traceId);
            return new XipkinEventImpl(eventId, x.parentEventIds);
        }
    }

    public static XipkinEventImpl of(EventId eventId, Set<EventId> parentEventIds) {
        return new XipkinEventImpl(eventId, parentEventIds);
    }

    @Override
    public XipkinEvent annotate(Object key, Object value) {
        annotations.put(key, value);
        return this;
    }

    @Override
    public XipkinEvent addParent(EventId parentEventId) {
        parentEventIds.add(parentEventId);
        return this;
    }

}
