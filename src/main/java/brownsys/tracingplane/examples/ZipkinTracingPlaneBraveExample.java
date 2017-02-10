package brownsys.tracingplane.examples;

import org.apache.log4j.BasicConfigurator;
import brave.Tracer;
import brownsys.tracingplane.opentracing.OpenTracingPlane.TracingPlaneSpan;
import brownsys.tracingplane.opentracing.zipkin.ZipkinTracingPlaneTracer;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

public class ZipkinTracingPlaneBraveExample {

    public static void main(String[] args) throws InterruptedException {
        BasicConfigurator.configure();
        
        OkHttpSender sender = OkHttpSender.create("http://127.0.0.1:9411/api/v1/spans");
        AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
        
        ZipkinTracingPlaneTracer tracer = ZipkinTracingPlaneTracer.wrap(Tracer.newBuilder()
                .localServiceName("my-tracingplane-service")
                .reporter(reporter)
                .build());
        
        
        TracingPlaneSpan root = tracer.buildSpan("tracingplane root span :)").start();
        root.log("hello world!");
        
        Thread.sleep(25);
        
        TracingPlaneSpan child = tracer.buildSpan("tracingplane child span").asChildOf(root).start();
        child.log("hello child!");
        
        Thread.sleep(100);
        
        child.finish();
        
        Thread.sleep(50);
        
        root.finish();
        
        Thread.sleep(50);
        reporter.close();
        sender.close();
        
        System.out.println("Done");

    }
}
