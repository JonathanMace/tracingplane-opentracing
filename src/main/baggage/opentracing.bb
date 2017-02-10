package brownsys.tracingplane.opentracing;


bag OpenTracingBaggage {
    map<string, string> fields = 0;
}

bag WrappedContexts {
    set<bytes> contexts = 0;
}