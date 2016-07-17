package de.skuzzle.inject.conf;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

class BeanUtil {

    private static final Map<Class<?>, Supplier<?>> COLLECTION_CONSTRUCTORS;
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

        COLLECTION_CONSTRUCTORS = new HashMap<>();
        COLLECTION_CONSTRUCTORS.put(Set.class, HashSet::new);
        COLLECTION_CONSTRUCTORS.put(List.class, ArrayList::new);
        COLLECTION_CONSTRUCTORS.put(Queue.class, ArrayDeque::new);
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

    public Collection<?> createCollection(Class<?> type) {
        if (type.isInterface()) {
            final Supplier constructor = COLLECTION_CONSTRUCTORS.get(type);
            checkArgument(constructor != null,
                    "Could not create a Collection instance of interface type '%s'. "
                            + "The framework does not know a default implementation for it.",
                    type.getName());
            return (Collection<?>) constructor.get();
        } else {
            // collection implementations should normally all have a default ctor
            try {
                return (Collection<?>) type.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalArgumentException(String.format(
                        "Could not create a Collection instance of type '%s'. "
                                + "Error while invoking its default constructor",
                        type.getName()));
            }
        }
    }

    public Class<?> getReturnTypeParameter(Method method) {
        final ParameterizedType paramType = (ParameterizedType) method
                .getGenericReturnType();
        return (Class<?>) paramType.getActualTypeArguments()[0];
    }
}
