package brownsys.tracingplane.zipkin;

struct ZipkinFlags {
    bool sample_attempted;
    bool sampled;
    bool debug;
}

struct ZipkinMetadata {
    // once baggage buffers supports optional values, make this optional
    fixed64 trace_id_high;
    fixed64 trace_id;
    fixed64 span_id;
    fixed64 parent_id;
    ZipkinFlags flags;
}

bag ZipkinContext {
    ZipkinMetadata metadata = 0; // Zipkin disallows multiple parents and simply discards any excess metadata
} 