package io.xipkin;

import edu.brown.cs.systems.tracingplane.baggage_buffers.BaggageLocals;
import io.opentracing.Span;

/**
 * This class provides static accessor methods for creating Xipkin tracers.
 * 
 * If you are new to instrumenting your system, be careful which tracer you pick! {@link XipkinTracer} provides two
 * fundamentally different choices for how contexts are propagated in your system.
 * 
 * 1) Deprecated: use OpenTracing APIs for context propagation. That is, use the following methods for passing around
 * contexts:
 * <ul>
 * <li>
 * </ul>
 * 
 * They have fundamentally different opinions on how to propagate context, so be careful about which Tracer to pick.
 * 
 */
public class Xipkin {

    /** How is context going to be propagated -- using OpenTracing APIs or TracingPlane APIs? */
    public static enum PropagationType {
                                        OpenTracing, TracingPlane
    }

    /** How is the 'active span' going to be managed -- explicitly by the user, or attached to Baggage? */
    public static enum SpanManagementType {
                                           Explicit, Automatic
    }

    private Xipkin() {}

    public static XipkinTracerBuilder newBuilder() {
        return new XipkinTracerBuilder();
    }

    /** Used to build a {@link XipkinTracer} with different options for how context is propagated */
    public static class XipkinTracerBuilder {

        private PropagationType propagation = PropagationType.TracingPlane;
        private SpanManagementType spanManagement = SpanManagementType.Automatic;

        private XipkinTracerBuilder() {}

        public XipkinTracerBuilder withPropagation(PropagationType propagationType) {
            this.propagation = propagationType;
            return this;
        }

        /**
         * Set the tracer context propagation to be done using TracingPlane apis (e.g., {@link Baggage#get()},
         * {@link Baggage#set(Baggage)}, etc.)
         */
        public XipkinTracerBuilder withTracingPlanePropagation() {
            return withPropagation(PropagationType.TracingPlane);
        }

        /**
         * Set the tracer context propagation to be done using OpenTracing apis (e.g., {@link Span#context()},
         * {@link io.opentracing.Tracer#extract(io.opentracing.propagation.Format, Object), etc.)
         */
        public XipkinTracerBuilder withOpenTracingPropagation() {
            return withPropagation(PropagationType.OpenTracing);
        }
        
        public XipkinTracerBuilder withSpanManagement(SpanManagementType spanManagementType) {
            this.spanManagement = spanManagementType;
            return this;
        }
        
        /**
         * Set the tracer to automatically manage spans using the {@link BaggageLocals}
         */
        public XipkinTracerBuilder withSpanManagement() {
            return withSpanManagement(SpanManagementType.Automatic);
        }

    }

}
