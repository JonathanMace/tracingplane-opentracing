package io.xipkin.opentracing;

import brownsys.tracingplane.opentracing.TracingPlanePropagation;
import brownsys.tracingplane.opentracing.TracingPlanePropagation.Registry;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.xipkin.impl.XipkinSpanBuilder;
import io.xipkin.impl.XipkinSpanContext;

/**
 * XipkinTracer implements OpenTracing because its APIs are so close.
 * 
 * However, OpenTracing should NOT be used for context propagation. TracingPlane Transit Layer should be used instead.
 *
 */
public class XipkinTracerImpl implements io.opentracing.Tracer {

    private final Registry registry = TracingPlanePropagation.defaults();

    @Override
    public SpanBuilder buildSpan(String spanName) {
        return new XipkinSpanBuilder(this, spanName);
    }

    /**
     * XipkinTracer is only OpenTracing compliant in order to be backwards compatible with systems that are OpenTracing
     * instrumented. However, instrumentation should otherwise be done using TracingPlane Transit Layer APIs.
     */
    @Override
    @Deprecated
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return new XipkinSpanContext(Baggage.deserialize(registry.extract(format, carrier)));
    }

    /**
     * XipkinTracer is only OpenTracing compliant in order to be backwards compatible with systems that are OpenTracing
     * instrumented. However, instrumentation should otherwise be done using TracingPlane Transit Layer APIs.
     */
    @Override
    @Deprecated
    public <C> void inject(SpanContext context, Format<C> format, C carrier) {
        if (!(context instanceof XipkinSpanContext)) {
            throw new RuntimeException("XipkinTracer is not compatible with unknown SpanContext " + context);
        }
        byte[] bytes = Baggage.serialize(((XipkinSpanContext) context).baggage);
        registry.inject(format, bytes, carrier);
    }

}
