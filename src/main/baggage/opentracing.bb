package edu.brown.cs.systems.tracingplane.opentracing;


bag OpenTracingBaggage {
    map<string, string> fields = 0;
}

bag OpenTracingContexts {
    set<bytes> contexts = 0;
}

struct HTraceID {
    fixed64 upper;
    fixed64 lower;
}

bag HTraceContext {
    set<HTraceID> parents = 0;
}



bag ZipkinFlags {
    taint sampled = 0;
}

bag ZipkinMetadata {
    fixed64 trace_id = 1;
    set<fixed64> span_ids = 2;
    set<fixed64> parent_span_ids = 3;
    ZipkinFlags flags = 4;
}
