package de.skuzzle.inject.conf;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

class PropertiesProxy implements InvocationHandler {

    private final Properties properties;
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
    }

    PropertiesProxy(Properties properties) {
        this.properties = properties;
    }

    private String getPropertyName(String methodName) {
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

    private Object coerce(String value, Class<?> targetType) {
        final Function<String, ?> parser = PARSERS.get(targetType);
        return parser.apply(value);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String propertyName = getPropertyName(method.getName());
        final Object result = this.properties.get(propertyName);
        if (result == null) {
            throw new IllegalStateException(String.format("Key '%s' not found",
                    propertyName));
        }
        if (result instanceof String) {
            return coerce(result.toString(), method.getReturnType());
        }
        return result;
    }
}
