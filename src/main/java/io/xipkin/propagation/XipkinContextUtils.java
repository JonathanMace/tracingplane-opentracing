package io.xipkin.propagation;

import java.util.function.Function;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import io.xipkin.EventId;
import io.xipkin.XipkinContext;
import io.xipkin.XipkinEvent;
import io.xipkin.XipkinSpan;
import io.xipkin.impl.NoopXipkinEvent;

/**
 * This is the only place where {@link XipkinContext} and {@link Baggage} should be interacted with.
 * 
 * All logic related to what context values means is implemented here
 */
public class XipkinContextUtils {

    private XipkinContextUtils() {}

    /**
     * Creates a new {@link XipkinSpan}, linking it to previous spans and events based on {@link EventId} instances
     * present in the {@link Baggage}, e.g., based on {@link XipkinContext#parentEventIds} and
     * {@link CurrentXipkinSpan#get()}.
     * 
     * The new {@link XipkinSpan} is created by invoking {@code constructor} provided to this function as an argument.
     * {@code constructor will be provided with the newly generated {@link EventId} for the span and should return a
     * {@link XipkinSpan} object.
     * 
     * If {@link XipkinContext#parentEventIds} is empty and {@link CurrentXipkinSpan#get()} returns null, or if tracing
     * has been turned off by some other configuration, then this method will bypass the provided {@code constructor}
     * and instead return {@link NoopXipkinSpan#instance}.
     * 
     * @param constructor a function that will be passed a {@link EventId} and returns a {@link XipkinSpan}; the
     *            simplest use case is to just pass XipkinSpanImpl::create
     * @return a new XipkinEvent instance
     */
    public static XipkinSpan newSpan(Function<EventId, XipkinSpan> constructor) {
        // If there is no current span in a thread local, create a new span using the propagated baggage parent ids as
        // parents
        // If there is a current span in the thread local, create an event on it prior to creating the child
        // Set the new span in the thread local
        return null;
    }

    /**
     * Creates a new {@link XipkinEvent}, linking it to previous events based on {@link EventId} instances present in
     * the {@link Baggage} (e.g., assuming one or more {@link EventId} is present in
     * {@link XipkinContext#parentEventIds}). After calling this method, {@link XipkinContext#parentEventIds} will be
     * updated to contain exactly one element, the newly generated {@link EventId}.
     * 
     * The new {@link XipkinEvent} is created by invoking {@code constructor} provided to this function as an argument.
     * {@code constructor} will be provided with the newly generated {@link EventId} and should return a
     * {@link XipkinEvent} object.
     * 
     * If {@link XipkinContext#parentEventIds} is empty and {@link CurrentXipkinSpan#get()} returns null, or if tracing
     * has been turned off by some other configuration, then this method will bypass the provided {@code constructor}
     * and instead return {@link NoopXipkinEvent#instance}.
     * 
     * @param constructor a function that will be passed a {@link EventId} and returns a {@link XipkinEvent}; the
     *            simplest use case is to just pass XipkinEventImpl::create
     * @return a new XipkinEvent instance
     */
    public static XipkinEvent newEvent(Function<EventId, XipkinEvent> constructor) {
        return null;
        // Get the union of event ids in the baggage + the current span's most recent event ID (if there is a current
        // span)
        // Create a new event
        // Set the most recent event ID + parent event IDs

        // XipkinContext x = XipkinContext.get();
        // if (x == null || x.parentEventIds == null || x.parentEventIds.isEmpty()) {
        // return NoopXipkinEvent.instance;
        // } else {
        // XipkinSpan currentSpan = CurrentXipkinSpan.get();
        // if (currentSpan != null) {
        // // The current thread already has a span -- this means any propagated event IDs should be linked to THAT
        // span, not the new span.
        // currentSpan = currentSpan.event();
        // }
        // Set<EventId> parentEvents = x.parentEventIds;
        // EventId eventId = new EventId();
        // }
    }

    /**
     * @return true if we're currently propagating some valid XipkinMetadata that can be used to create spans and events
     */
    public static boolean isValid() {
        XipkinContext x = XipkinContext.get();
        if (x == null || x.parentEventIds == null || x.parentEventIds.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

}
