package edu.brown.cs.systems.tracingplane.opentracing;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.io.BaseEncoding;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.propagation.TextMap;

public class TracingPlanePropagation {
    
    static final Logger log = LoggerFactory.getLogger(OpenTracingPlane.class);

    @FunctionalInterface
    public static interface Extractor<C> {
        byte[] extract(C carrier);
    }

    @FunctionalInterface
    public static interface Injector<C> {
        void inject(byte[] payload, C carrier);
    }

    public static final class BinaryPropagation {
        private BinaryPropagation() {}
        public static final Format<ByteBuffer> FORMAT = Builtin.BINARY;
        public static final Injector<ByteBuffer> INJECTOR = (bs, buf) -> buf.put(bs);
        public static final Extractor<ByteBuffer> EXTRACTOR = buf -> {
            byte[] bs = new byte[buf.remaining()];
            buf.get(bs);
            return bs;
        };
    }

    public static final class TextMapPropagation {
        private TextMapPropagation() {}
        public static final Format<TextMap> FORMAT = Builtin.TEXT_MAP;
        public static final String KEY = "x-baggage";
        public static final BaseEncoding ENCODING = BaseEncoding.base64();
        public static final Injector<TextMap> INJECTOR = (bs, map) -> map.put(KEY, ENCODING.encode(bs));
        public static final Extractor<TextMap> EXTRACTOR = map -> {
            String value = findEntry(KEY, map.iterator());
            try{
                return ENCODING.decode(value);
            } catch(IllegalArgumentException e) {
                return null;
            }
        };
    }

    public static final class HTTPHeaderPropagation {
        private HTTPHeaderPropagation() {}
        public static final Format<TextMap> FORMAT = Builtin.HTTP_HEADERS;
        public static final String KEY = "x-baggage";
        public static final BaseEncoding ENCODING = BaseEncoding.base64Url();
        public static final Injector<TextMap> INJECTOR = (bs, map) -> map.put(KEY, ENCODING.encode(bs));
        public static final Extractor<TextMap> EXTRACTOR = map -> {
            String value = findEntry(KEY, map.iterator());
            try{
                return ENCODING.decode(value);
            } catch(IllegalArgumentException e) {
                return null;
            }
        };
    }

    /** Utility method: finds an entry with a specific key from an iterator */
    private static String findEntry(String key, Iterator<Map.Entry<String, String>> it) {
        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            if (entry != null && entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static final Registry defaultRegistryInstance = new Registry(){
        protected void initialize() {
            injectors.put(BinaryPropagation.FORMAT, BinaryPropagation.INJECTOR);
            injectors.put(TextMapPropagation.FORMAT, TextMapPropagation.INJECTOR);
            injectors.put(HTTPHeaderPropagation.FORMAT, HTTPHeaderPropagation.INJECTOR);
            
            extractors.put(BinaryPropagation.FORMAT, BinaryPropagation.EXTRACTOR);
            extractors.put(TextMapPropagation.FORMAT, TextMapPropagation.EXTRACTOR);
            extractors.put(HTTPHeaderPropagation.FORMAT, HTTPHeaderPropagation.EXTRACTOR);
        }
    };

    public static Registry defaults() {
        return defaultRegistryInstance;
    }

    /** A registry of injectors and extractors.*/
    public static abstract class Registry {

        protected final Map<Format<?>, Injector<?>> injectors = new HashMap<>();
        protected final Map<Format<?>, Extractor<?>> extractors = new HashMap<>();

        private Registry() {}

        /** This method registers all injectors and extractors */
        protected abstract void initialize();
        
        public Injector<?> injector(Format<?> format) throws UnsupportedFormatException {
            Injector<?> injector = injectors.get(format);
            if (injector == null) {
                throw new UnsupportedFormatException(format, "No injector for " + format);
            }
            return injector;
        }
        
        public Extractor<?> extractor(Format<?> format) throws UnsupportedFormatException {
            Extractor<?> extractor = extractors.get(format);
            if (extractor == null) {
                throw new UnsupportedFormatException(format, "No extractor for " + format);
            }
            return extractor;
        }
        
        @SuppressWarnings("unchecked")
        public <C> byte[] extract(Format<C> format, C carrier) {
            try {
                return ((Extractor<C>) extractor(format)).extract(carrier);
            } catch (UnsupportedFormatException e) {
                handleUnsupportedFormat(e);
                return null;
            }
        }
        
        @SuppressWarnings("unchecked")
        public <C> void inject(Format<C> format, byte[] payload, C carrier) {
            try {
                ((Injector<C>) injector(format)).inject(payload, carrier);
            } catch (UnsupportedFormatException e) {
                handleUnsupportedFormat(e);
            }
        }
        
        protected static final Set<Format<?>> unsupportedFormatsEncountered = new HashSet<>();
        protected static synchronized void handleUnsupportedFormat(UnsupportedFormatException e) {
            if (!unsupportedFormatsEncountered.contains(e.format)) {
                unsupportedFormatsEncountered.add(e.format);
                log.error("Cannot extract or inject tracing contexts from unsupported format " + e.format, e);
            }
        }

    }
    
    public static final class UnsupportedFormatException extends Exception {
        private static final long serialVersionUID = 2884858988852957949L;
        public final Format<?> format;
        public UnsupportedFormatException(Format<?> format, String message) {
            super(message);
            this.format = format;
        }
    }

}
