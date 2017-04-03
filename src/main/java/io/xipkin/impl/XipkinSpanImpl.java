package io.xipkin.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.xipkin.XipkinEvent;
import io.xipkin.impl.AbstractXipkinSpan;

public class XipkinSpanImpl extends AbstractXipkinSpan {

    private final XipkinEvent startEvent;
    private List<XipkinEvent> events = new ArrayList<>();

    public XipkinSpanImpl() {
        this.startEvent = null; // TODO: fix
    }

    @Override
    public void close() {
        
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finish(long arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Span log(Map<String, ?> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Span log(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Span log(long arg0, Map<String, ?> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Span log(long arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Span log(String arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Span log(long arg0, String arg1, Object arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Span setOperationName(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Span setTag(String key, String value) {
        zipkin.Span span;
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Span setTag(String arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Span setTag(String arg0, Number arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    private String toAnnotation(Map<String, ?> fields) {
        if (fields.size() == 1 && fields.containsKey("event")) {
            return String.valueOf(fields.get("event"));
        } else {
            return fields.entrySet().stream()
                         .map(p -> String.format("%s=%s ", p.getKey(), p.getValue()))
                         .reduce("", String::concat);
        }
    }

}
