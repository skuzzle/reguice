package de.skuzzle.inject.conf;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.inject.ProvisionException;

class JsonContentType implements TextContentType {

    private final BeanUtil beanUtil;

    JsonContentType(BeanUtil beanUtil) {
        this.beanUtil = beanUtil;
    }

    @Override
    public <T> T createInstance(Class<T> type, TextResource resource) {
        final Gson gson = new Gson();
        try (Reader reader  = resource.openStream()) {

            if (type.isInterface()) {
                final JsonParser parser = new JsonParser();
                final JsonElement element = parser.parse(reader);

                checkArgument(element.isJsonObject(),
                        "top level JSON element must be an object");

                final InvocationHandler handler = new JsonProxy(
                        element.getAsJsonObject(), this.beanUtil);
                return type.cast(Proxy.newProxyInstance(type.getClassLoader(),
                        new Class[] { type }, handler));
            }

            return gson.fromJson(reader, type);
        } catch (final IOException e) {
            throw new ProvisionException("Error while deserializing from json", e);
        }
    }

}
