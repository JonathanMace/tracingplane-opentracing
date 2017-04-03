package io.xipkin.impl;

import java.util.Collections;
import java.util.Map.Entry;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;
import io.xipkin.Xipkin;
import io.xipkin.XipkinContext;
import io.xipkin.XipkinSpan;
import io.xipkin.opentracing.XipkinTracer;

public class XipkinSpanBuilder implements SpanBuilder {

    private final XipkinTracer xipkin;
    private final String spanName;

    public XipkinSpanBuilder(XipkinTracer xipkin, String spanName) {
        this.xipkin = xipkin;
        this.spanName = spanName;
    }

    @Override
    public Iterable<Entry<String, String>> baggageItems() {
        XipkinContext x = XipkinContext.get();
        if (x != null && x.opentracingBaggage != null) {
            return x.opentracingBaggage.entrySet();
        }
        return Collections.emptySet();
    }

    /**
     * Should use TracingPlane {@link TransitLayer} methods instead of opentracing propagation, e.g.,
     * {@link Baggage#join(Baggage)}
     */
    @Deprecated
    @Override
    public SpanBuilder addReference(String referenceType, SpanContext ctx) {
        if (ctx instanceof XipkinSpanContext) {
            Baggage.join(((XipkinSpanContext) ctx).baggage);
        }
        return this;
    }

    /**
     * Should use TracingPlane {@link TransitLayer} methods instead of opentracing propagation, e.g.,
     * {@link Baggage#join(Baggage)}
     */
    @Deprecated
    @Override
    public SpanBuilder asChildOf(SpanContext ctx) {
        return addReference(References.CHILD_OF, ctx);
    }

    @Override
    public SpanBuilder asChildOf(Span span) {
        
    }

    @Override
    public Span start() {
        if (!Xipkin.isValid()) {
            return new NoopXipkinSpan();
        }
        XipkinEventImpl startEvent = XipkinEventImpl.generate();
        if (startEvent == null) {
            return new NoopXipkinSpan();
        }
        return new XipkinSpan(startEvent);
    }

    @Override
    public SpanBuilder withStartTimestamp(long arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpanBuilder withTag(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpanBuilder withTag(String arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpanBuilder withTag(String arg0, Number arg1) {
        // TODO Auto-generated method stub
        return null;
    }

}
