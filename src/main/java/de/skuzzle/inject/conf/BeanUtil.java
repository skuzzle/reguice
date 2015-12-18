package de.skuzzle.inject.conf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class BeanUtil {

    private static final Map<Class<?>, Function<Number, ?>> NUMBER_MAPPER;
    static {
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

    public Object coerceType(String value, Class<?> targetType) {
        return Converters.parseString(targetType, value);
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
            return methodName;
        }

        if (prefixLen == methodName.length()) {
            return methodName;
        }
        final StringBuilder b = new StringBuilder(methodName.length());
        final String part = methodName.substring(prefixLen);
        b.append(part);
        b.setCharAt(0, Character.toLowerCase(part.charAt(0)));
        return b.toString();
    }
}
