package brownsys.tracingplane.htrace;

struct HTraceID {
    fixed64 upper;
    fixed64 lower;
}

bag HTraceContext {
    set<HTraceID> parents = 0;
}

