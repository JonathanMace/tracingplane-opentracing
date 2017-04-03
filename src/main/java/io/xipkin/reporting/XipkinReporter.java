package io.xipkin.reporting;

import io.xipkin.XipkinSpan;
import io.xipkin.XipkinTracer;
import io.xipkin.impl.XipkinEventImpl;

/**
 * The interface for reporting spans or events. These methods are called in-band when spans are closed, so a reporter
 * should probably enqueue them and asynchronously send them.
 */
public interface XipkinReporter {

    /**
     * Report a {@link XipkinSpan} along with all of the events it contains
     */
    public void reportSpan(XipkinSpan span);

    /**
     * We do not necessarily need a {@link XipkinSpan} in order to log a {@link XipkinEventImpl}. This method is called
     * when the user logs an event directly on the {@link XipkinTracer} without an active span being present.
     */
    public void reportStandaloneEvent(XipkinEventImpl event);

}
