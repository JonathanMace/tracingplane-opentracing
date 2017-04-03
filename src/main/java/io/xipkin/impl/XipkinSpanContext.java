package io.xipkin.impl;

import java.util.Collections;
import java.util.Map.Entry;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import io.xipkin.XipkinContext;

/**
 * This only exists to be OpenTracing compliant. TracingPlane Transit Layer propagation should handle context
 * propagation.
 */
@Deprecated
class XipkinSpanContext implements io.opentracing.SpanContext {

    final Baggage baggage;

    XipkinSpanContext(Baggage baggage) {
        this.baggage = baggage;
    }

    @Override
    public Iterable<Entry<String, String>> baggageItems() {
        XipkinContext x = XipkinContext.getFrom(baggage);
        if (x != null && x.opentracingBaggage != null) {
            return x.opentracingBaggage.entrySet();
        }
        return Collections.emptySet();
    }

}