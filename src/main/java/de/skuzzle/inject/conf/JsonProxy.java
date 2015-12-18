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
        checkArgument(args == null, "mapped methods must not have parameters");
        final String propertyName = this.beanUtil.getPropertyName(method.getName());
        final JsonElement element = this.root.get(propertyName);

        return getObject(propertyName, element, method.getReturnType());
    }

    private Object getObject(String memberName, JsonElement element,
            Class<?> targetType) {
        return this.objectMap.computeIfAbsent(memberName,
                key -> coerce(targetType, element));
    }

    private Object coerce(Class<?> targetType, JsonElement element) {
        if (element == null) {
            return null;
        } else if (element.isJsonPrimitive()) {
            return coercePrimitive(targetType, element.getAsJsonPrimitive());
        } else if (element.isJsonNull()) {
            return null;
        } else if (element.isJsonArray()) {
            return coerceArray(targetType, element.getAsJsonArray());
        }  else if (element.isJsonObject()) {
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

    private Object coerceArray(Class<?> targetType, JsonArray array) {
        checkArgument(targetType.isArray());
        final Object arr = Array.newInstance(targetType.getComponentType(),
                array.size());
        for (int i = 0; i < array.size(); ++i) {
            final Object elem = coerce(targetType.getComponentType(),
                    array.get(i));
            Array.set(arr, i, elem);
        }
        return arr;
    }

    private Object coerceObject(Class<?> targetType, JsonObject object) {
        checkArgument(targetType.isInterface());
        final InvocationHandler handler = new JsonProxy(object, this.beanUtil);
        final ClassLoader cl = getClass().getClassLoader();
        return Proxy.newProxyInstance(cl, new Class[] { targetType }, handler);
    }
}
