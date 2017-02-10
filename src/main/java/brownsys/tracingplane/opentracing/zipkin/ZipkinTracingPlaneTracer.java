package brownsys.tracingplane.opentracing.zipkin;

import java.util.Map;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import brownsys.tracingplane.opentracing.OpenTracingPlane.TracingPlaneSpan;
import brownsys.tracingplane.opentracing.OpenTracingPlane.TracingPlaneSpanBuilder;
import brownsys.tracingplane.opentracing.OpenTracingPlane.TracingPlaneSpanContext;
import brownsys.tracingplane.opentracing.OpenTracingPlane.TracingPlaneTracer;
import brownsys.tracingplane.zipkin.ZipkinContext;
import brownsys.tracingplane.zipkin.ZipkinMetadata;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

public class ZipkinTracingPlaneTracer extends TracingPlaneTracer {
    
    private final brave.Tracer braveTracer;
    
    private ZipkinTracingPlaneTracer(brave.Tracer braveTracer) {
        this.braveTracer = braveTracer;
    }
    
    public static ZipkinTracingPlaneTracer wrap(brave.Tracer braveTracer) {
        return new ZipkinTracingPlaneTracer(braveTracer);
    }

    @Override
    public TracingPlaneSpanBuilder buildSpan(String spanName) {
        return new SpanBuilder(spanName);
    }
    
    public ZipkinContext fromBrave(brave.propagation.TraceContext braveContext) {
        if (braveContext == null) {
            return null;
        }
        ZipkinContext ctx = new ZipkinContext();
        ctx.metadata = new ZipkinMetadata();
        ctx.metadata.traceIdHigh = braveContext.traceIdHigh();
        ctx.metadata.traceId = braveContext.traceId();
        ctx.metadata.spanId = braveContext.spanId();
        ctx.metadata.parentId = braveContext.parentId();
        ctx.metadata.flags.debug = braveContext.debug();
        ctx.metadata.flags.sampleAttempted = braveContext.sampled() != null;
        ctx.metadata.flags.sampled = braveContext.sampled() == null ? false : braveContext.sampled();
        return ctx;
    }
    
    public brave.propagation.TraceContext toBrave(ZipkinContext ctx) {
        if (ctx == null || ctx.metadata == null) {
            return null;
        }
        return brave.propagation.TraceContext.newBuilder()
                    .traceIdHigh(ctx.metadata.traceIdHigh)
                    .traceId(ctx.metadata.traceId)
                    .spanId(ctx.metadata.spanId)
                    .parentId(ctx.metadata.parentId)
                    .debug(ctx.metadata.flags.debug)
                    .sampled(ctx.metadata.flags.sampleAttempted ? ctx.metadata.flags.sampled : null)
                    .build();
    }
    
    public class Span extends TracingPlaneSpan {

        private brave.Span braveSpan;

        public Span(Baggage baggage, brave.Span braveSpan) {
            super(baggage);
            this.braveSpan = braveSpan;
        }
        
        @Override
        public TracingPlaneSpanContext context() {
            // Branch the baggage we carry and update the zipkin context
            Baggage branched = ZipkinContext.setIn(Baggage.branch(baggage), fromBrave(braveSpan.context()));
            return TracingPlaneSpanContext.wrap(branched);
        }

        @Override public void close() { braveSpan.finish(); }
        @Override public void finish() { braveSpan.finish(); }
        @Override public void finish(long finishMicros) { braveSpan.finish(finishMicros); }
        
        @Override
        public TracingPlaneSpan setTag(String key, String value) {
            braveSpan.tag(key, value);
            return this;
        }

        @Override
        public TracingPlaneSpan setTag(String key, boolean value) {
            return setTag(key, String.valueOf(value));
        }

        @Override
        public TracingPlaneSpan setTag(String key, Number value) {
            return setTag(key, String.valueOf(value));
        }
        
        private String toAnnotation(Map<String, ?> fields) {
            if (fields.size() == 1 && fields.containsKey("event")) {
                return String.valueOf(fields.get("event"));
            } else {
                return fields.entrySet().stream()
                             .map(p -> String.format("%s=%s", p.getKey(), p.getValue()))
                             .reduce("", String::concat);
            }
        }

        @Override
        public TracingPlaneSpan log(Map<String, ?> fields) {
            if (fields != null) {
                return log(toAnnotation(fields));
            } else {
                return this;
            }
        }

        @Override
        public TracingPlaneSpan log(long timestampMicroseconds, Map<String, ?> fields) {
            if (fields != null) {
                return log(timestampMicroseconds, toAnnotation(fields));
            } else {
                return this;
            }
        }

        @Override
        public TracingPlaneSpan log(String event) {
            braveSpan.annotate(event);
            return this;
        }

        @Override
        public TracingPlaneSpan log(long timestampMicroseconds, String event) {
            braveSpan.annotate(timestampMicroseconds, event);
            return this;
        }

        @Override
        public TracingPlaneSpan log(String eventName, Object payload) {
            braveSpan.annotate(eventName);
            return this;
        }

        @Override
        public TracingPlaneSpan log(long timestampMicroseconds, String eventName, Object payload) {
            braveSpan.annotate(timestampMicroseconds, eventName);
            return this;
        }

        @Override
        public TracingPlaneSpan setOperationName(String operationName) {
            braveSpan.name(operationName);
            return this;
        }
        
    }
    
    
    public class SpanBuilder extends TracingPlaneSpanBuilder {
        
        private String operationName;
        private long startTimestampMicroseconds = 0;
        private final Multimap<String, String> tags = HashMultimap.create();
        
        SpanBuilder(String operationName) {
            this.operationName = operationName;
        }

        @Override
        public TracingPlaneSpan start() {
            // Extract and translate context from baggage to brave
            brave.propagation.TraceContext parent = toBrave(ZipkinContext.getFrom(baggage));
            baggage = ZipkinContext.setIn(baggage, null);
            
            brave.Span span = parent == null ? braveTracer.newTrace() : braveTracer.newChild(parent);
            
            if (operationName != null) {
                span.name(operationName);
            }
            
            for (Map.Entry<String, String> tag : tags.entries()) {
                span.tag(tag.getKey(), tag.getValue());
            }
            
            if (startTimestampMicroseconds == 0) {
                return new Span(baggage, span.start());
            } else {
                return new Span(baggage, span.start(startTimestampMicroseconds));
            }
        }

        @Override
        public TracingPlaneSpanBuilder withTag(String key, String value) {
            tags.put(key, value);
            return this;
        }

        @Override
        public TracingPlaneSpanBuilder withTag(String key, boolean value) {
            return withTag(key, String.valueOf(value));
        }

        @Override
        public TracingPlaneSpanBuilder withTag(String key, Number value) {
            return withTag(key, String.valueOf(value));
        }

        @Override
        public TracingPlaneSpanBuilder withStartTimestamp(long microseconds) {
            this.startTimestampMicroseconds = microseconds;
            return this;
        }
        
    }

}
