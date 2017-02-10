package brownsys.tracingplane.examples;

import org.apache.log4j.BasicConfigurator;
import brave.Tracer;
import brownsys.tracingplane.opentracing.OpenTracingPlane.TracingPlaneSpan;
import brownsys.tracingplane.opentracing.zipkin.ZipkinTracingPlaneTracer;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

public class ZipkinTracingPlaneBraveExample {
    
    static OkHttpSender sender = OkHttpSender.create("http://127.0.0.1:9411/api/v1/spans");
    static AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
    
    static ZipkinTracingPlaneTracer tracer = ZipkinTracingPlaneTracer.wrap(Tracer.newBuilder()
            .localServiceName("my-tracingplane-service")
            .reporter(reporter)
            .build());
    
    public static class InstrumentedThread extends Thread {
        
        public final String name;
        public TracingPlaneSpan parent, mySpan;
        
        public InstrumentedThread(TracingPlaneSpan parent, String name) {
            this.parent = parent;
            this.name = name;
        }
        
        public void run() {
            mySpan = tracer.buildSpan("thread-"+name).asChildOf(parent).start();
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            
            mySpan.setBaggageItem(name, name + " value");
            
            mySpan.finish();
        }
        
    }

    public static void main(String[] args) throws InterruptedException {
        BasicConfigurator.configure();
        
        
        TracingPlaneSpan root = tracer.buildSpan("tracingplane root span :)").start();
        root.log("hello world!");
        
        Thread.sleep(25);
        
        TracingPlaneSpan child = tracer.buildSpan("tracingplane child span").asChildOf(root).start();
        child.log("hello child!");
        
        Thread.sleep(100);

        InstrumentedThread t1 = new InstrumentedThread(child, "t1");
        InstrumentedThread t2 = new InstrumentedThread(child, "t2");
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();

        // Print the baggage from T1 and T2 respectively
        System.out.println("\nT1 baggage: \n" + t1.mySpan.context().get());
        System.out.println("\nT2 baggage: \n" + t2.mySpan.context().get());
        
        // Merge the final contents of T1 and T2 back into their parent
        child.mergeWith(t1.mySpan.baggage());
        child.mergeWith(t2.mySpan.baggage());
        System.out.println("\nChild baggage: \n" + child.context().get());
        
        Thread.sleep(20);
        
        child.finish();
        
        // Merge the final contents of child back into its parent
        root.mergeWith(child.baggage());
        System.out.println("\nRoot baggage: \n" + root.context().get());
        
        Thread.sleep(50);
        
        root.finish();
        
        Thread.sleep(50);
        reporter.close();
        sender.close();
        
        System.out.println("Done");

    }
}
