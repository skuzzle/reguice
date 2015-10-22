package de.skuzzle.inject.conf;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

class PropertiesProxy implements InvocationHandler {

    private final BeanUtil util;
    private final Map<Object, Object> properties;

    PropertiesProxy(Map<Object, Object> properties, BeanUtil beanUtil) {
        this.properties = properties;
        this.util = beanUtil;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        checkArgument(args == null, "mapped methods must not have parameters");
        final String propertyName = this.util.getPropertyName(method.getName());
        final Object result = this.properties.get(propertyName);
        if (result == null) {
            throw new IllegalStateException(String.format("Key '%s' not found",
                    propertyName));
        }
        if (result instanceof String) {
            return this.util.coerceSimpleType(result.toString(), method.getReturnType());
        }
        return result;
    }
}
