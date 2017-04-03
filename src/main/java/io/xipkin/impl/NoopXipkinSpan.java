package io.xipkin.impl;

import java.util.Map;
import io.opentracing.Span;

public class NoopXipkinSpan extends AbstractXipkinSpan {

    @Override
    public void close() {}

    @Override
    public void finish() {}

    @Override
    public void finish(long arg0) {}

    @Override
    public Span log(Map<String, ?> arg0) {
        return this;
    }

    @Override
    public Span log(String arg0) {
        return this;
    }

    @Override
    public Span log(long arg0, Map<String, ?> arg1) {
        return this;
    }

    @Override
    public Span log(long arg0, String arg1) {
        return this;
    }

    @Override
    public Span log(String arg0, Object arg1) {
        return this;
    }

    @Override
    public Span log(long arg0, String arg1, Object arg2) {
        return this;
    }

    @Override
    public Span setOperationName(String arg0) {
        return this;
    }

    @Override
    public Span setTag(String arg0, String arg1) {
        return this;
    }

    @Override
    public Span setTag(String arg0, boolean arg1) {
        return this;
    }

    @Override
    public Span setTag(String arg0, Number arg1) {
        return this;
    }

    @Override
    public Span event() {
        return this;
    }

}
