package de.skuzzle.inject.conf;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Function;

import javax.inject.Provider;
import javax.servlet.ServletContext;

import com.google.gson.GsonBuilder;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;

import de.skuzzle.inject.conf.Resources.ChooseBufferType;
import de.skuzzle.inject.conf.Resources.ChooseContentType;
import de.skuzzle.inject.conf.Resources.ChooseContentTypeAndCharset;
import de.skuzzle.inject.conf.Resources.ChooseResources;
import de.skuzzle.inject.conf.Resources.ChooseTargetType;
import de.skuzzle.inject.conf.Resources.Finalize;
import de.skuzzle.inject.conf.Resources.FinalizeWithScope;

final class DSLImpl implements
        ChooseBufferType,
        ChooseResources,
        ChooseContentType,
        ChooseContentTypeAndCharset,
        ChooseTargetType {

    // either of these 2 fields will be nonnull
    private TextContentType contentType;
    private Class<? extends TextContentType> contentTypeType;

    private Charset charset;
    private Function<Charset, TextResource> resourceFactory;
    private MutableProvider<ServletContext> servletCtxProvider;
    // null until set
    private CachingStrategy cacheStrategy;

    private final TextResourceFactory textResourceFactory;
    private final ContentTypeFactory contentTypeFactory;

    DSLImpl(TextResourceFactory factory, ContentTypeFactory contentTypeFactory) {
        this.textResourceFactory = factory;
        this.contentTypeFactory = contentTypeFactory;
    }

    private static class MutableProvider<T> implements Provider<T> {
        private Provider<T> realProvider;

        void set(Provider<T> t) {
            this.realProvider = t;
        }

        @Override
        public T get() {
            checkState(this.realProvider != null, "ServletContext not available");
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
        public T create() {
            checkArgument(DSLImpl.this.contentType != null);
            return create(() -> DSLImpl.this.contentType);
        }

        @SuppressWarnings("unchecked")
        private T create(Provider<TextContentType> contentTypeProvider) {
            final TextResource resource = createResource();
            final Class<T> targetType = (Class<T>) this.targetKey
                    .getTypeLiteral().getRawType();
            final TextContentType contentTypeInst = contentTypeProvider.get();
            return contentTypeInst.createInstance(targetType, resource);
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

            final Provider<TextContentType> contentTypeProvider;
            if (DSLImpl.this.contentType != null) {
                binder.requestInjection(DSLImpl.this.contentType);
                contentTypeProvider = () -> DSLImpl.this.contentType;
            } else {
                checkState(DSLImpl.this.contentTypeType != null,
                        "either explicit content type instance or " +
                        "content type class must be specified");
                contentTypeProvider = (Provider<TextContentType>) binder.getProvider(
                        DSLImpl.this.contentTypeType);
            }

            final Provider<T> provider = () -> create(contentTypeProvider);
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

    private TextResource createResource() {
        final TextResource root = DSLImpl.this.resourceFactory.apply(
                DSLImpl.this.charset);

        if (this.cacheStrategy != null) {
            return this.textResourceFactory.cache(root, this.cacheStrategy);
        }

        return root;
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
    public <T> FinalizeWithScope<T> to(Class<T> type, String name) {
        checkArgument(type != null, "type is null");
        checkArgument(name != null, "name is null");
        return new FinalizeImpl<>(Key.get(type, Names.named(name)));
    }

    @Override
    public ChooseTargetType containingJson() {
        this.contentType = this.contentTypeFactory.newJsonContentType(new GsonBuilder());
        return this;
    }

    @Override
    public ChooseTargetType containingJson(GsonBuilder builder) {
        checkArgument(builder != null, "builder is null");
        this.contentType = this.contentTypeFactory.newJsonContentType(builder);
        return this;
    }

    @Override
    public ChooseTargetType containingXml() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public ChooseTargetType containingText() {
        this.contentType = this.contentTypeFactory.newStringContentType();
        return this;
    }

    @Override
    public ChooseTargetType containingProperties() {
        this.contentType = this.contentTypeFactory.newPropertiesContentType();
        return this;
    }

    @Override
    public ChooseTargetType containing(TextContentType contentType) {
        checkArgument(contentType != null, "contentType is null");
        this.contentType = contentType;
        return this;
    }

    @Override
    public ChooseTargetType containing(Class<? extends TextContentType> contentTypeType) {
        checkArgument(contentTypeType != null, "contentTypeType is null");
        this.contentTypeType = contentTypeType;
        return this;
    }

    @Override
    public ChooseContentType encodedWith(Charset charset) {
        checkArgument(charset != null, "charset is null");
        this.charset = charset;
        return this;
    }

    @Override
    public ChooseContentType encodedWith(String charset) {
        checkArgument(charset != null, "charset is null");
        this.charset = Charset.forName(charset);
        return this;
    }

    @Override
    public ChooseContentType encodedWithSystemDefaultCharset() {
        this.charset = Charset.defaultCharset();
        return this;
    }

    @Override
    public ChooseContentType encodedWithProvidedCharset() {
        this.charset = null;
        return this;
    }

    @Override
    public ChooseContentTypeAndCharset classPathResource(String path) {
        checkArgument(path != null, "path is null");
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        this.resourceFactory = cs -> this.textResourceFactory.newClassPathResource(
                path, cl, cs);
        return this;
    }

    @Override
    public ChooseContentTypeAndCharset classPathResource(String path, ClassLoader cl) {
        checkArgument(path != null, "path is null");
        checkArgument(cl != null, "cl is null");
        this.resourceFactory = cs -> this.textResourceFactory.newClassPathResource(
                path, cl, cs);
        return this;
    }

    @Override
    public ChooseContentTypeAndCharset servletResource(String path) {
        checkArgument(path != null, "path is null");
        this.servletCtxProvider = new MutableProvider<ServletContext>();
        this.resourceFactory = cs -> this.textResourceFactory.newServletResource(path,
                this.servletCtxProvider, cs);
        return this;
    }

    @Override
    public ChooseContentTypeAndCharset fileResource(File file) {
        checkArgument(file != null, "file is null");
        this.resourceFactory = cs -> this.textResourceFactory.newNioResource(
                file.toPath(), cs);
        return this;
    }

    @Override
    public ChooseContentTypeAndCharset pathResource(Path path) {
        checkArgument(path != null, "path is null");
        this.resourceFactory = cs -> this.textResourceFactory.newNioResource(path, cs);
        return this;
    }

    @Override
    public ChooseContentTypeAndCharset urlResource(URL url) {
        checkArgument(url != null, "url is null");
        this.resourceFactory = cs -> this.textResourceFactory.newURLResource(url, cs);
        return this;
    }

    @Override
    public ChooseContentTypeAndCharset urlResource(String url) {
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
    public ChooseContentTypeAndCharset resource(TextResource resource) {
        checkArgument(resource != null, "resource is null");
        this.resourceFactory = cs -> resource;
        return this;
    }

    @Override
    public ChooseResources cached(CachingStrategy strategy) {
        checkArgument(strategy != null, "strategy is null");
        this.cacheStrategy = strategy;
        return this;
    }

    @Override
    public ChooseResources buffered() {
        this.cacheStrategy = new TimestampCacheStrategy();
        return this;
    }

    @Override
    public ChooseResources reloadable() {
        this.cacheStrategy = null;
        return this;
    }

    @Override
    public ChooseResources constant() {
        this.cacheStrategy = ConstantCacheStrategy.getInstance();
        return this;
    }
}
