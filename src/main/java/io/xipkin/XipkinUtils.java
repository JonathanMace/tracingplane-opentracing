package io.xipkin;

import java.util.Random;

public class XipkinUtils {

    // TODO: update this with a better library or seeds
    private static final Random r = new Random();

    public static EventId nextTraceId() {
        return nextSpanId(r.nextLong());
    }

    public static EventId nextSpanId(long traceId) {
        return nextEventId(traceId, r.nextLong());
    }

    public static EventId nextEventId(long traceId, long spanId) {
        EventId id = new EventId();
        id.traceId = traceId;
        id.spanId = spanId;
        id.eventId = r.nextLong();
        return id;
    }

}
