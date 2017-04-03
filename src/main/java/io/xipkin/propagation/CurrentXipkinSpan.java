package io.xipkin.propagation;

import edu.brown.cs.systems.tracingplane.baggage_buffers.BaggageLocals;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import io.xipkin.XipkinSpan;

/**
 * Provides static methods for accessing a "currentSpan" object.
 * 
 * This class could be updated in future to use a stack, to support the 'push/pop' behavior that some instrumented
 * systems like to use.
 * 
 * Uses the {@link BaggageLocals} API to attach span objects to the propagated {@link Baggage} instance.
 */
public class CurrentXipkinSpan {

    // Key for accessing to the current span baggage attachment
    private static final Object CURRENT_SPAN = new Object();

    /**
     * Gets the {@link XipkinSpan} object that is attached to the current thread's {@link Baggage}.
     * 
     * @return the {@link XipkinSpan} attached to the current Baggage if there is one. If there is not one, returns
     *         null.
     */
    public static XipkinSpan get() {
        Object currentSpan = BaggageLocals.getAttachment(Baggage.get(), CURRENT_SPAN);
        if (currentSpan instanceof XipkinSpan) {
            return (XipkinSpan) currentSpan;
        } else {
            return null;
        }
    }

    /**
     * Set the {@link XipkinSpan} object that is attached to the current thread's {@link Baggage}, overwriting any
     * previous {@link XipkinSpan} that may have been attached.
     * 
     * @param span a {@link XipkinSpan} to set as the current span for the current thread's {@link Baggage}.
     */
    public static void set(XipkinSpan span) {
        Baggage.set(BaggageLocals.attachObject(Baggage.get(), CURRENT_SPAN, span));
    }

}
