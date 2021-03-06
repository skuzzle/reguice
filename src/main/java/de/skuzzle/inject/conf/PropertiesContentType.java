package de.skuzzle.inject.conf;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.ProvisionException;

class PropertiesContentType implements TextContentType {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesContentType.class);

    private final BeanUtil beanUtil;

    PropertiesContentType(BeanUtil beanUtil) {
        this.beanUtil = beanUtil;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createInstance(Class<T> type, TextResource resource) {
        final ClassLoader cl = type.getClassLoader();
        final Properties props = getProperties(resource);

        // bind directly to Map/Properties instance
        if (Map.class.isAssignableFrom(type)) {
            return type.cast(props);
        }

        checkArgument(type.isInterface(), "type must be an interface");
        final InvocationHandler handler = new PropertiesProxy(props, this.beanUtil);
        return (T) Proxy.newProxyInstance(cl, new Class[] { type }, handler);
    }

    private static Properties getProperties(TextResource resource) {
        try (Reader r = resource.openStream()) {
            final Properties result = new Properties();
            result.load(r);
            return result;
        } catch (final IOException e) {
            LOG.error("Error while locating/reading the resource '{}'",
                    resource, e);
            throw new ProvisionException(String.format(
                    "Error while locating/reading the resource '%s'",
                    resource), e);
        }
    }
}
