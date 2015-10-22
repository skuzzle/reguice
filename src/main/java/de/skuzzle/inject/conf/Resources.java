package de.skuzzle.inject.conf;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

import com.google.inject.Binder;
import com.google.inject.Key;


public interface Resources {

    public static ChoseBufferType bind() {
        final ResourceUtil util = new ResourceUtil();
        final BeanUtil beanUtil = new BeanUtil();
        final TextResourceFactory factory = new TextResourceFactoryImpl(util);
        final ContentTypeFactory contentTypeFactory = new ContentTypeFactoryImpl(
                beanUtil);
        return new DSLImpl(factory, contentTypeFactory);
    }

    interface ChoseBufferType extends ChoseResources {
        ChoseResources buffered();
        ChoseResources reloadable();
        ChoseResources constant();
    }

    interface ChoseResources {
        ChoseContentTypeAndCharset classPathResource(String path);
        ChoseContentTypeAndCharset classPathResource(String path, ClassLoader cl);
        ChoseContentTypeAndCharset servletResource(String path);
        ChoseContentTypeAndCharset fileResource(File file);
        ChoseContentTypeAndCharset pathResource(Path path);
        ChoseContentTypeAndCharset urlResource(URL url);
        ChoseContentTypeAndCharset urlResource(String url);
        ChoseContentType resource(TextResource resource);
    }

    interface ChoseContentTypeAndCharset extends ChoseContentType {
        ChoseContentType encodedWith(String charset);
        ChoseContentType encodedWith(Charset charset);
        ChoseContentType encodedWithSystemDefaultCharset();
        ChoseContentType encodedWithProvidedCharset();
    }

    interface ChoseContentType {
        ChoseTargetType containingText();
        ChoseTargetType containingJson();
        ChoseTargetType containingXml();
        ChoseTargetType containingProperties();
        ChoseTargetType containing(TextContentType contentType);
    }

    interface ChoseTargetType {
        <T> FinalizeWithScope<T> to(Key<T> key);
        <T> FinalizeWithScope<T> to(Class<T> type);
        <T> FinalizeWithScope<T> to(Class<T> type, String name);
    }

    interface FinalizeWithScope<T> extends Finalize<T> {
        Finalize<T> in(Class<? extends Annotation> scope);
    }

    interface Finalize<T> {
        void using(Binder binder);
    }
}
