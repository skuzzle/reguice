package de.skuzzle.inject.conf;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Function;

import javax.inject.Provider;
import javax.servlet.ServletContext;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.binder.ScopedBindingBuilder;

import de.skuzzle.inject.conf.Resources.ChoseBufferType;
import de.skuzzle.inject.conf.Resources.ChoseContentType;
import de.skuzzle.inject.conf.Resources.ChoseContentTypeAndCharset;
import de.skuzzle.inject.conf.Resources.ChoseResources;
import de.skuzzle.inject.conf.Resources.ChoseTargetType;
import de.skuzzle.inject.conf.Resources.Finalize;
import de.skuzzle.inject.conf.Resources.FinalizeWithScope;

final class DSLImpl implements ChoseBufferType,
        ChoseResources,
        ChoseContentType,
        ChoseContentTypeAndCharset,
        ChoseTargetType {

    private TextContentType contentType;
    private Charset charset;
    private Function<TextResource, TextResource> wrapper = Function.identity();
    private Function<Charset, TextResource> resourceFactory;

    // intentionally leave out type here to not force ServletContext to be on
    // class path
    private MutableProvider servletCtxProvider;

    private final TextResourceFactory textResourceFactory;

    DSLImpl(TextResourceFactory factory) {
        this.textResourceFactory = factory;
    }

    private static class MutableProvider<T> implements Provider<T> {
        private Provider<T> realProvider;

        void set(Provider<T> t) {
            this.realProvider = t;
        }

        @Override
        public T get() {
            return this.realProvider.get();
        }
    }

    private final class FinalizeImpl<T> implements FinalizeWithScope<T> {

        private Class<? extends Annotation> scope;
        private final Key<T> targetKey;

        FinalizeImpl(Key<T> targetKey) {
            this.targetKey = targetKey;
        }

        @Override
        public Finalize<T> in(Class<? extends Annotation> scope) {
            checkArgument(scope != null, "scope is null");
            this.scope = scope;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void using(Binder binder) {
            checkArgument(binder != null, "binder is null");

            final TextResource root = DSLImpl.this.resourceFactory.apply(
                    DSLImpl.this.charset);

            final TextResource actual = DSLImpl.this.wrapper.apply(root);
            final Class<T> targetType = (Class<T>) this.targetKey
                    .getTypeLiteral().getRawType();
            final Provider<T> provider = () -> DSLImpl.this.contentType.createInstance(
                    targetType, actual);

            if (DSLImpl.this.servletCtxProvider != null) {
                final Provider<ServletContext> realProvider = binder.getProvider(
                        ServletContext.class);
                DSLImpl.this.servletCtxProvider.set(realProvider);
            }
            final ScopedBindingBuilder builder = binder
                    .bind(this.targetKey)
                    .toProvider(provider);

            if (this.scope != null) {
                builder.in(this.scope);
            }
        }

    }

    @Override
    public <T> FinalizeWithScope<T> to(Key<T> key) {
        checkArgument(key != null, "key is null");
        return new FinalizeImpl<>(key);
    }

    @Override
    public <T> FinalizeWithScope<T> to(Class<T> type) {
        checkArgument(type != null, "type is null");
        return new FinalizeImpl<>(Key.get(type));
    }

    @Override
    public ChoseTargetType containingJson() {
        this.contentType = new JsonContentType();
        return this;
    }

    @Override
    public ChoseTargetType containingXml() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public ChoseTargetType containingProperties() {
        this.contentType = new PropertiesContentType();
        return this;
    }

    @Override
    public ChoseTargetType containing(TextContentType contentType) {
        checkArgument(contentType != null, "contentType is null");
        this.contentType = contentType;
        return this;
    }

    @Override
    public ChoseContentType encodedWith(Charset charset) {
        checkArgument(charset != null, "charset is null");
        this.charset = charset;
        return this;
    }

    @Override
    public ChoseContentType encodedWith(String charset) {
        checkArgument(charset != null, "charset is null");
        this.charset = Charset.forName(charset);
        return this;
    }

    @Override
    public ChoseContentType encodedWithSystemDefaultCharset() {
        this.charset = Charset.defaultCharset();
        return this;
    }

    @Override
    public ChoseContentType encodedWithProvidedCharset() {
        this.charset = null;
        return this;
    }

    @Override
    public ChoseContentTypeAndCharset classPathResource(String path) {
        checkArgument(path != null, "path is null");
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        this.resourceFactory = cs -> this.textResourceFactory.newClassPathResource(
                path, cl, cs);
        return this;
    }

    @Override
    public ChoseContentTypeAndCharset classPathResource(String path, ClassLoader cl) {
        checkArgument(path != null, "path is null");
        checkArgument(cl != null, "cl is null");
        this.resourceFactory = cs -> this.textResourceFactory.newClassPathResource(
                path, cl, cs);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChoseContentTypeAndCharset servletResource(String path) {
        checkArgument(path != null, "path is null");
        this.servletCtxProvider = new MutableProvider<ServletContext>();
        this.resourceFactory = cs -> this.textResourceFactory.newServletResource(path,
                this.servletCtxProvider, cs);
        return this;
    }

    @Override
    public ChoseContentTypeAndCharset fileResource(File file) {
        checkArgument(file != null, "file is null");
        this.resourceFactory = cs -> this.textResourceFactory.newNioResource(
                file.toPath(), cs);
        return this;
    }

    @Override
    public ChoseContentTypeAndCharset pathResource(Path path) {
        checkArgument(path != null, "path is null");
        this.resourceFactory = cs -> this.textResourceFactory.newNioResource(path, cs);
        return this;
    }

    @Override
    public ChoseContentTypeAndCharset urlResource(URL url) {
        checkArgument(url != null, "url is null");
        this.resourceFactory = cs -> this.textResourceFactory.newURLResource(url, cs);
        return this;
    }

    @Override
    public ChoseContentTypeAndCharset urlResource(String url) {
        checkArgument(url != null, "url is null");
        try {
            final URL u = new URL(url);
            this.resourceFactory = cs -> this.textResourceFactory.newURLResource(u, cs);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        return this;
    }

    @Override
    public ChoseContentTypeAndCharset resource(TextResource resource) {
        checkArgument(resource != null, "resource is null");
        this.resourceFactory = cs -> resource;
        return this;
    }

    @Override
    public ChoseResources buffered() {
        this.wrapper = BufferedTextResource::wrap;
        return this;
    }

    @Override
    public ChoseResources reloadable() {
        this.wrapper = Function.identity();
        return this;
    }

    @Override
    public ChoseResources constant() {
        this.wrapper = ConstantTextResource::wrap;
        return this;
    }
}
