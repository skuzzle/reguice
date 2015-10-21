package de.skuzzle.inject.conf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class BeanUtil {

    private static final Map<Class<?>, Function<String, ?>> PARSERS;
    private static final Map<Class<?>, Function<Number, ?>> NUMBER_MAPPER;
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

        NUMBER_MAPPER = new HashMap<>();
        NUMBER_MAPPER.put(Byte.class, Number::byteValue);
        NUMBER_MAPPER.put(Byte.TYPE, Number::byteValue);
        NUMBER_MAPPER.put(Short.class, Number::shortValue);
        NUMBER_MAPPER.put(Short.TYPE, Number::shortValue);
        NUMBER_MAPPER.put(Integer.class, Number::intValue);
        NUMBER_MAPPER.put(Integer.TYPE, Number::intValue);
        NUMBER_MAPPER.put(Long.class, Number::longValue);
        NUMBER_MAPPER.put(Long.TYPE, Number::longValue);
        NUMBER_MAPPER.put(Float.class, Number::floatValue);
        NUMBER_MAPPER.put(Float.TYPE, Number::floatValue);
        NUMBER_MAPPER.put(Double.class, Number::doubleValue);
        NUMBER_MAPPER.put(Double.TYPE, Number::doubleValue);
    }

    public Object coerceSimpleType(String value, Class<?> targetType) {
        final Function<String, ?> parser = PARSERS.get(targetType);
        return parser.apply(value);
    }

    public Object coerceNumber(Number number, Class<?> targetType) {
        final Function<Number, ?> mapper = NUMBER_MAPPER.get(targetType);
        return mapper.apply(number);
    }

    public String getPropertyName(String methodName) {
        final int prefixLen;
        if (methodName.startsWith("get")) {
            prefixLen = 3;
        } else if (methodName.startsWith("is")) {
            prefixLen = 2;
        } else {
            throw new IllegalArgumentException(String.format(
                    "'%s' is not a valid getter name", methodName));
        }
        final StringBuilder b = new StringBuilder(methodName.length());
        final String part = methodName.substring(prefixLen);
        b.append(part);
        b.setCharAt(0, Character.toLowerCase(part.charAt(0)));
        return b.toString();
    }
}
