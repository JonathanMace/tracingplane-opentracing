package brownsys.tracingplane.examples;

import brave.Tracer;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

public class ZipkinBraveExample {
    
    
    public static void main(String[] args) throws InterruptedException {
        OkHttpSender sender = OkHttpSender.create("http://127.0.0.1:9411/api/v1/spans");
        AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
        
        Tracer tracer = Tracer.newBuilder()
                .localServiceName("my-service")
                .reporter(reporter)
                .build();
        
        
        brave.Span root = tracer.newTrace().name("root span :)").annotate("Hello world!").start();
        
        Thread.sleep(25);
        
        brave.Span child = tracer.newChild(root.context()).name("child spannn").start();
        
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
