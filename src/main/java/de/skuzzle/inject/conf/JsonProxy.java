package de.skuzzle.inject.conf;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

class JsonProxy implements InvocationHandler {

    private final JsonObject root;
    private final Map<String, Object> objectMap;
    private final BeanUtil beanUtil;

    JsonProxy(JsonObject root, BeanUtil beanUtil) {
        this.root = root;
        this.beanUtil = beanUtil;
        this.objectMap = new HashMap<>();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        checkArgument(args == null, "mapped methods must not have parameters");
        final String propertyName = this.beanUtil.getPropertyName(method.getName());
        final JsonElement element = this.root.get(propertyName);

        return getObject(propertyName, element, method.getReturnType(), method);
    }

    private Object getObject(String memberName, JsonElement element,
            Class<?> targetType, Method method) {
        return this.objectMap.computeIfAbsent(memberName,
                key -> coerce(targetType, element, method));
    }

    private Object coerce(Class<?> targetType, JsonElement element, Method method) {
        if (element == null) {
            return null;
        } else if (element.isJsonPrimitive()) {
            return coercePrimitive(targetType, element.getAsJsonPrimitive());
        } else if (element.isJsonNull()) {
            return null;
        } else if (element.isJsonArray()) {
            return coerceArray(targetType, element.getAsJsonArray(), method);
        } else if (element.isJsonObject()) {
            return coerceObject(targetType, element.getAsJsonObject());
        }
        throw new AssertionError("not reachable");
    }

    private Object coercePrimitive(Class<?> targetType, JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            checkArgument(targetType == Boolean.class || targetType == Boolean.TYPE);
            return primitive.getAsBoolean();
        } else if (primitive.isNumber()) {
            return this.beanUtil.coerceNumber(primitive.getAsNumber(), targetType);
        } else if (primitive.isString()) {
            return this.beanUtil.coerceType(primitive.getAsString(),
                    targetType);
        }
        throw new AssertionError();
    }

    private Object coerceArray(Class<?> targetType, JsonArray array, Method method) {
        if (Collection.class.isAssignableFrom(targetType)) {
            return coerceCollection(targetType, array, method);
        }

        checkArgument(targetType.isArray());
        final Object arr = Array.newInstance(targetType.getComponentType(),
                array.size());
        for (int i = 0; i < array.size(); ++i) {
            final Object elem = coerce(targetType.getComponentType(),
                    array.get(i), method);
            Array.set(arr, i, elem);
        }
        return arr;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object coerceCollection(Class<?> targetType, JsonArray array, Method method) {
        checkArgument(Collection.class.isAssignableFrom(targetType));
        final Collection result = this.beanUtil.createCollection(targetType);
        final Class<?> elementType = this.beanUtil.getReturnTypeParameter(method);
        for (int i = 0; i < array.size(); ++i) {
            final Object elem = coerce(elementType,
                    array.get(i), method);
            checkArgument(elementType.isInstance(elem));
            result.add(elem);
        }
        return result;
    }

    private Object coerceObject(Class<?> targetType, JsonObject object) {
        checkArgument(targetType.isInterface());
        final InvocationHandler handler = new JsonProxy(object, this.beanUtil);
        final ClassLoader cl = getClass().getClassLoader();
        return Proxy.newProxyInstance(cl, new Class[] { targetType }, handler);
    }
}
