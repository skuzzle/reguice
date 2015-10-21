package de.skuzzle.inject.conf;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
        final String propertyName = this.beanUtil.getPropertyName(method.getName());
        final JsonElement element = this.root.get(propertyName);
        return getObject(propertyName, element, method.getReturnType());
    }

    private Object getObject(String memberName, JsonElement element,
            Class<?> targetType) {
        return this.objectMap.computeIfAbsent(memberName,
                key -> coerceToObject(targetType, element));
    }

    private Object coerceToObject(Class<?> targetType, JsonElement element) {
        if (element == null) {
            return null;
        } else if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                checkArgument(targetType == Boolean.class || targetType == Boolean.TYPE);
                return primitive.getAsBoolean();
            } else if (primitive.isNumber()) {
                return this.beanUtil.coerceNumber(primitive.getAsNumber(), targetType);
            } else if (primitive.isString()) {
                checkArgument(targetType.isAssignableFrom(String.class));
                return primitive.getAsString();
            }
        } else if (element.isJsonNull()) {
            return null;
        } else if (element.isJsonArray()) {
            checkArgument(targetType.isArray());
            final JsonArray array = element.getAsJsonArray();
            final Object arr = Array.newInstance(targetType.getComponentType(),
                    array.size());
            for (int i = 0; i < array.size(); ++i) {
                final Object elem = coerceToObject(targetType.getComponentType(),
                        array.get(i));
                Array.set(arr, i, elem);
            }
            return arr;
        }  else if (element.isJsonObject()) {
            checkArgument(targetType.isInterface());
            final InvocationHandler handler = new JsonProxy(
                    element.getAsJsonObject(), this.beanUtil);
            final ClassLoader cl = getClass().getClassLoader();
            return Proxy.newProxyInstance(cl, new Class[] { targetType }, handler);
        }
        throw new AssertionError("not reachable");
    }
}
