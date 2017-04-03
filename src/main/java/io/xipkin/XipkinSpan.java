package io.xipkin;

import java.util.TreeMap;
import edu.brown.cs.systems.tracingplane.baggage_buffers.BaggageBuffers;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;
import io.opentracing.Span;
import io.opentracing.SpanContext;

/**
 * An OpenTracing-compliant Span implementation for Xipkin.
 * 
 * OpenTracing fundamentally combines two ideas: context propagation to link spans together; and construction and
 * logging of spans.
 * 
 * Xipkin is backed by the Tracing Plane, which means context propagation is ideally provided by {@link TransitLayer}
 * rather than OpenTracing.
 * 
 * Thus the OpenTracing methods that do context propagation -- eg, methods receiving and returning {@link SpanContext}
 * objects -- have been marked as deprecated to indicate that they should not be used.
 * 
 * However, if those methods <b>are</b> used, they will nonetheless correctly wrap the underlying {@link Baggage}
 * carried by the {@link TransitLayer}. This means Xipkin can be dropped into systems that were previously instrumented
 * with OpenTracing.
 */
public abstract class XipkinSpan implements io.opentracing.Span {

    /**
     * Should use TracingPlane {@link TransitLayer} methods instead of opentracing propagation, e.g.,
     * {@link Baggage#join(Baggage)}
     */
    @Deprecated
    @Override
    public final SpanContext context() {
        return new XipkinSpanContext(Baggage.get());
    }

    /**
     * Use {@link BaggageBuffers} directly instead of OpenTracing baggage
     */
    @Deprecated
    @Override
    public final String getBaggageItem(String key) {
        XipkinContext x = XipkinContext.get();
        if (x != null && x.opentracingBaggage != null) {
            return x.opentracingBaggage.get(key);
        }
        return null;
    }

    /**
     * Use {@link BaggageBuffers} directly instead of OpenTracing baggage
     */
    @Deprecated
    @Override
    public final Span setBaggageItem(String key, String value) {
        XipkinContext x = XipkinContext.get();
        if (x == null) x = new XipkinContext();
        if (x.opentracingBaggage == null) x.opentracingBaggage = new TreeMap<>();
        x.opentracingBaggage.put(key, value);
        return this;
    }

    /**
     * This is a method that {@link io.opentracing.Span} should have but does not.
     * 
     * It is nonetheless deprecated here...
     * 
     * Should use TracingPlane {@link TransitLayer} methods instead of opentracing propagation, e.g.,
     * {@link Baggage#join(Baggage)}
     * 
     * After merging baggage if you have an active span call {@link #event()}
     */
    @Deprecated
    public final Span addReference(String referenceType, SpanContext ctx) {
        if (ctx instanceof XipkinSpanContext) {
            Baggage.join(((XipkinSpanContext) ctx).baggage);
        }
        event();
        return this;
    }

    /**
     * This is like calling {@link XipkinSpan#log(String)} but with no message. It might log an event and might not,
     * depending on whether parent event IDs have updated and are interesting
     */
    public abstract Span event();

}
