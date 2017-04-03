package io.xipkin;

struct EventId {
    fixed64 trace_id;
    fixed64 span_id;
    fixed64 event_id;
}

bag XipkinContext {
    set<EventId> parent_event_ids = 0;
    map<string, string> opentracing_baggage = 1;
} 