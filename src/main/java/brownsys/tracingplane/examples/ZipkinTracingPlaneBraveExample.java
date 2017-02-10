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
        
        // Print the root span's baggage as of now
        System.out.println("Root baggage: \n" + root.context().get());
        
        Thread.sleep(25);
        
        TracingPlaneSpan child = tracer.buildSpan("tracingplane child span").asChildOf(root).start();
        child.log("hello child!");

        // Print the child span's baggage as of now
        System.out.println("\nChild baggage: \n" + child.context().get());
        
        Thread.sleep(100);

        // Start some threads that make concurrent spans.  Each thread will put a k-v pair into the baggage
        InstrumentedThread t1 = new InstrumentedThread(child, "t1");
        InstrumentedThread t2 = new InstrumentedThread(child, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        // The threads are now done, let's inspect their baggage
        // Print the baggage from T1 and T2 respectively
        System.out.println("\nT1 baggage: \n" + t1.mySpan.context().get());
        System.out.println("\nT2 baggage: \n" + t2.mySpan.context().get());
        
        // Let's propagate their baggage back to their parent span
        // Merge the final contents of T1 and T2 back into their parent
        child.mergeWith(t1.mySpan.baggage());
        child.mergeWith(t2.mySpan.baggage());
        
        // Print the current state of the child's baggage now that it merged with T1 and T2
        System.out.println("\nChild baggage: \n" + child.context().get());
        
        Thread.sleep(20);
        
        child.finish();
        
        // Let's propagate the child's baggage back to its parent
        // Merge the final contents of child back into its parent
        root.mergeWith(child.baggage());
        
        // Print the current state of the root baggage
        System.out.println("\nRoot baggage: \n" + root.context().get());
        
        Thread.sleep(50);
        
        root.finish();
        
        Thread.sleep(50);
        reporter.close();
        sender.close();
        
        System.out.println("Done");

    }
}
