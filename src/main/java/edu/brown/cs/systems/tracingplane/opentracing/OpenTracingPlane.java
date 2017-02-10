package edu.brown.cs.systems.tracingplane.opentracing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;

/**
 * Extends OpenTracing interfaces with some baggage methods
 */
public interface OpenTracingPlane {

    /**
     * An OpenTracing SpanContext that is backed by a TracingPlane Baggage object.
     * 
     * This class is not intended to be overridden. Any Tracer-specific identifiers that live in its SpanContext must be
     * carried in Baggage.
     */
    public final class TracingPlaneSpanContext implements SpanContext {

        private Baggage baggage;

        private TracingPlaneSpanContext() {}

        /**
         * Create a new TracingPlaneSpanContext with no contents
         */
        public final static TracingPlaneSpanContext empty() {
            return new TracingPlaneSpanContext();
        }

        /**
         * Create a new TracingPlaneSpanContext that wraps the provided baggage
         */
        public final static TracingPlaneSpanContext wrap(Baggage baggage) {
            TracingPlaneSpanContext ctx = new TracingPlaneSpanContext();
            ctx.baggage = baggage;
            return ctx;
        }

        /**
         * @return the baggage carried in this span context without branching
         */
        public Baggage get() {
            return baggage;
        }

        /**
         * Update the baggage carried by this context. Usually follows a call to a {@code setIn()} method
         */
        public void set(Baggage baggage) {
            this.baggage = baggage;
        }

        /**
         * Merge the baggage carried in this context with the baggage carried by another context
         * 
         * @param other another context to merge with
         * @return a possibly new context containing the merged baggages
         */
        public TracingPlaneSpanContext mergeWith(TracingPlaneSpanContext other) {
            if (other == null || other.baggage == null) {
                return this;
            } else if (baggage == null) {
                return other;
            } else {
                return mergeWith(other.baggage);
            }
        }

        /**
         * Merge the baggage carried in this context with the baggage provided
         * 
         * @param baggage other baggage to merge in
         * @return a possibly new context containing the merged baggages
         */
        public TracingPlaneSpanContext mergeWith(Baggage baggage) {
            if (baggage == null) {
                return this;
            } else {
                return wrap(Baggage.join(this.baggage, baggage));
            }
        }

        @Override
        public Iterable<Entry<String, String>> baggageItems() {
            OpenTracingBaggage otb = OpenTracingBaggage.getFrom(baggage);
            if (otb != null && otb.fields != null) {
                return otb.fields.entrySet();
            } else {
                return Collections.emptySet();
            }
        }

    }

    /**
     * Maintains an instance of Baggage.
     * 
     * Provides a default implementation of getting and setting OpenTracing "baggage" (key-value pairs)
     */
    public abstract class TracingPlaneSpan implements Span {

        protected Baggage baggage = null;

        protected TracingPlaneSpan() {
            this(null);
        }

        protected TracingPlaneSpan(Baggage baggage) {
            this.baggage = baggage;
        }

        /**
         * Default behavior just branches the baggage.
         * 
         * Override this if you wish to set additional fields in the baggage before creating a new context
         */
        @Override
        public TracingPlaneSpanContext context() {
            return TracingPlaneSpanContext.wrap(Baggage.branch(baggage));
        }

        /**
         * @return the baggage carried in this span context without branching
         */
        public final Baggage peekBaggage() {
            return baggage;
        }

        /**
         * Update the baggage carried by this context. Usually follows a call to a {@code setIn()} method
         */
        public final void updateBaggage(Baggage baggage) {
            this.baggage = baggage;
        }

        @Override
        public final String getBaggageItem(String key) {
            OpenTracingBaggage otb = OpenTracingBaggage.getFrom(baggage);
            if (otb != null && otb.fields != null) {
                return otb.fields.get(key);
            } else {
                return null;
            }
        }

        @Override
        public final Span setBaggageItem(String key, String value) {
            OpenTracingBaggage otb = OpenTracingBaggage.getFrom(baggage);
            if (otb == null) {
                otb = new OpenTracingBaggage();
            }
            if (otb.fields == null) {
                otb.fields = new HashMap<>();
            }
            otb.fields.put(key, value);
            baggage = OpenTracingBaggage.setIn(baggage, otb);
            return this;
        }

    }

    /**
     * A span builder that merges in baggage from reference spans.
     */
    public abstract class TracingPlaneSpanBuilder implements SpanBuilder {

        protected Baggage baggage = null;

        protected TracingPlaneSpanBuilder() {
            this(null);
        }

        protected TracingPlaneSpanBuilder(Baggage baggage) {
            this.baggage = baggage;
        }

        @Override
        public final Iterable<Entry<String, String>> baggageItems() {
            OpenTracingBaggage otb = OpenTracingBaggage.getFrom(baggage);
            if (otb != null && otb.fields != null) {
                return otb.fields.entrySet();
            } else {
                return Collections.emptySet();
            }
        }

        /**
         * Default implementation merges the referenced baggage, and ignores span contexts that are not baggage based
         * 
         * Override this method to change behavior (eg, referenceType-specific behavior)
         */
        @Override
        public TracingPlaneSpanBuilder addReference(String referenceType, SpanContext reference) {
            if (reference instanceof TracingPlaneSpanContext) {
                return withBaggage(((TracingPlaneSpanContext) reference).baggage);
            } else {
                return this;
            }
        }

        @Override
        public TracingPlaneSpanBuilder asChildOf(SpanContext reference) {
            return addReference(References.CHILD_OF, reference);
        }

        @Override
        public TracingPlaneSpanBuilder asChildOf(Span reference) {
            return addReference(References.CHILD_OF, reference.context());
        }

        /**
         * By default, merge the other baggage with the baggage carried in this span builder
         */
        public TracingPlaneSpanBuilder withBaggage(Baggage otherBaggage) {
            baggage = Baggage.join(baggage, Baggage.branch(otherBaggage));
            return this;
        }

        /**
         * At this point, all parent baggages have been merged in.
         * 
         * The internal {@link #baggage} instance can now be consulted for parent IDs, etc., to create a new span.
         */
        @Override
        public abstract TracingPlaneSpan start();

    }

}