== Tracing Plane / OpenTracing ==

This repository provides some examples and implementations for using the Tracing Plane's context propagation specification in conjunction with OpenTracing.

=== Why do this? ===

The Tracing Plane lets us construct arbitrary objects and efficiency pack them into a serialized format.  It has built-in semantics that enable a variety of different data types, and automatically supports correctly merging contexts if your tracing system allows multiple parents.

See the [Tracing Plane GitHub](https://github.com/JonathanMace/tracingplane) for more information about the Tracing Plane

=== Examples ===

==== Tracing Plane Based Tracers ====

[OpenTracingPlane.java](https://github.com/JonathanMace/tracingplane-opentracing/blob/master/src/main/java/brownsys/tracingplane/opentracing/OpenTracingPlane.java) provides a base implementation of an OpenTracing tracer that is backed by Baggage as its serialization format.  Non-binary carriers simply wrap the binary-encoded baggage (eg, in base64); for example, HTTP headers will set the `X-Baggage' field.

==== Zipkin / Brave Tracer, backed by Tracing Plane ====

[ZipkinTracingPlaneTracer.java](https://github.com/JonathanMace/tracingplane-opentracing/blob/master/src/main/java/brownsys/tracingplane/opentracing/zipkin/ZipkinTracingPlaneTracer.java) is a concrete tracer implementation that wraps Zipkin's [Brave](https://github.com/openzipkin/brave) instrumentation library.  The tracer is an OpenTracing tracer (so, OpenTracing has generalized the instrumentation) and it's backed by Baggage (so, Baggage has generalized the context propagation), so the only component of Brave remaining is the concrete construction and backend-aggregation of span objects.

[ZipkinTracingPlaneBraveExample.java](https://github.com/JonathanMace/tracingplane-opentracing/blob/master/src/main/java/brownsys/tracingplane/examples/ZipkinTracingPlaneBraveExample.java) is a simple working example of trace construction using this tracingplane-backed tracer.

[zipkin.bb](https://github.com/JonathanMace/tracingplane-opentracing/blob/master/src/main/baggage/zipkin.bb) is the Baggage Buffers specification of Zipkin's metadata.  It's intended as a like-for-like replica of Zipkin's metadata format, so it will automatically drop multiple parents if they exist.

	package brownsys.tracingplane.zipkin;

	struct ZipkinFlags {
	    bool sample_attempted;
	    bool sampled;
	    bool debug;
	}

	struct ZipkinMetadata {
	    fixed64 trace_id_high;
	    fixed64 trace_id;
	    fixed64 span_id;
	    fixed64 parent_id;
	    ZipkinFlags flags;
	}

	bag ZipkinContext {
	    ZipkinMetadata metadata = 0; // Zipkin disallows multiple parents and simply discards any excess metadata
	} 

==== OpenTracing Key-Value Pairs Baggage ====

In addition to regular zipkin metadata, we have also provided a default implementation of OpenTracing's key-value pair Baggage (not to be confused with tracingplane Baggage which is a more generic concept)

[opentracing.bb](https://github.com/JonathanMace/tracingplane-opentracing/blob/master/src/main/baggage/opentracing.bb) is the Baggage Buffers specification of baggage.

	package brownsys.tracingplane.opentracing;


	bag OpenTracingBaggage {
	    map<string, string> fields = 0;
	}


==== Generic Wrapper for Tracers ====

TODO: a generic wrapper tracer that just byte-serializes the other tracer's contexts

==== Pure Tracing Plane Tracer ====

TODO: a tracer that creates zipkin spans for the backend, but specifies its own metadata format and consistently handles relationships between spans, using just annotations for now :/
