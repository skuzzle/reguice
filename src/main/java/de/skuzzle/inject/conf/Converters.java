package de.skuzzle.inject.conf;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Converters {

    private static final Logger LOG = LoggerFactory.getLogger(Converters.class);

    private static final Map<Class<?>, Converter<?>> CONVERTERS;
    private static final Map<Class<?>, Function<String, ?>> PARSERS;

    static {
        PARSERS = new HashMap<>();
        PARSERS.put(Byte.class, Byte::parseByte);
        PARSERS.put(Byte.TYPE, Byte::parseByte);
        PARSERS.put(Short.class, Short::parseShort);
        PARSERS.put(Short.TYPE, Short::parseShort);
        PARSERS.put(Integer.class, Integer::parseInt);
        PARSERS.put(Integer.TYPE, Integer::parseInt);
        PARSERS.put(Long.class, Long::parseLong);
        PARSERS.put(Long.TYPE, Long::parseLong);
        PARSERS.put(Float.class, Float::parseFloat);
        PARSERS.put(Float.TYPE, Float::parseFloat);
        PARSERS.put(Double.class, Double::parseDouble);
        PARSERS.put(Double.TYPE, Double::parseDouble);
        PARSERS.put(Boolean.class, Boolean::parseBoolean);
        PARSERS.put(Boolean.TYPE, Boolean::parseBoolean);
        PARSERS.put(String.class, Function.identity());
        PARSERS.put(Object.class, Function.identity());

        @SuppressWarnings("rawtypes")
        final ServiceLoader<Converter> converters = ServiceLoader.load(Converter.class);
        CONVERTERS = new HashMap<>();
        converters.forEach(converter -> {
            final Class<?> type = converter.forType();
            LOG.debug("Registering converter {} for {}", converter, type.getName());
            final Converter<?> previous = CONVERTERS.put(type, converter);
            if (previous != null) {
                LOG.warn("Duplicated converter mapping. Converter {} for type {} has " +
                    "been overridden with converter {}", previous, type.getName(),
                    converter);
            }
        });
    }

    private Converters() {
        // hidden constructor
    }

    @SuppressWarnings("unchecked")
    public static <T> T parseString(Class<T> targetType, String s) {
        final Function<String, T> prim = (Function<String, T>) PARSERS.get(targetType);
        if (prim != null) {
            return prim.apply(s);
        }
        final Converter<T> converter = (Converter<T>) CONVERTERS.get(targetType);
        checkState(converter != null, "No conversion for String '%s' to %s available",
                s, targetType.getName());
        return converter.parseString(s);
    }
}
