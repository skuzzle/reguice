package de.skuzzle.inject.conf;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

import javax.servlet.ServletContext;

import com.google.gson.GsonBuilder;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;

/**
 * Allows to bind resources from various sources with various content types.
 * This class is typically be used within a {@link Module} where you have access
 * to a {@link Binder} instance. It provides an <em>Embedded Domain Specific
 * Language</em> (EDSL) for configuring the binding. The starting point is the
 * static method {@link #bind()}. The last action for every resource to be bound
 * is to call {@link Finalize#using(Binder)} which will actually publish the
 * configuration to the passed Binder.
 * <p>
 * The process of binding a resource consists of several steps that are
 * implemented as EDSL so it is nearly impossible to get it wrong. As mentioned
 * above its mandatory to always reach step 7 because otherwise your
 * configuration is not applied to the Binder. <b>There is no programmatic way
 * of noticing a forgotten call to {@link Finalize#using(Binder)}.</b>
 * <ol>
 * <li>
 * <b>Choosing a buffer type</b><em> (optional)</em><br/>
 * You may chose between 3 different kinds of buffering the content of the
 * resource to be bound:
 * <ul>
 * <li><em>constant</em> - The resource will be read once and is then buffered
 * in memory. Further accesses to the resource will always return the in-memory
 * view.</li>
 * <li><em>buffered</em> - Same as constant but the resource will be rebuffered
 * if a change of its {@link BinaryResource#getLastModifiedTime() modification
 * date} is detected.</li>
 * <li><em>reloadable</em> (default) - Every access to the resource will read it
 * again.</li>
 * </ul>
 * Depending on the scope (which is configured later, see step 6) not every
 * buffer type will make sense. If your resource is bound as {@link Singleton}
 * there is no need to buffer it, as it is anyway read only once.</li>
 * <li>
 * <b>Choosing the source</b><br/>
 * During this mandatory step the source of the resource is specified. The
 * resource can be read from the class path, the file system, from an
 * {@link URL} or from a {@link ServletContext}. You can also directly provide
 * an instance of {@link TextResource} here.</li>
 * <li>
 * <b>Choosing the encoding</b> <em>(Optional)</em><br/>
 * Reading characters from a binary source <em>always</em> requires an encoding.
 * There a several possibilities of specifying the encoding for the resource:
 * <ul>
 * <li><em>explicitly</em> - you pass the desired encoding either by name or as
 * {@link Charset} instance.</li>
 * <li><em>system</em> - the system default encoding as returned by
 * {@link Charset#defaultCharset()} will be used.</li>
 * <li><em>provided</em> (default) - Some resources come with embedded content
 * types like for example HTTP resources. Determining the content type might
 * involve creating extra requests/connections to the actual resource. If an
 * encoding can not be determined the implementation falls back to the system's
 * default encoding.</li>
 * </ul>
 * Note that usage of a system's default encoding makes the behavior of your
 * application system dependent. Thus this should be avoided in favor of
 * explicit specification of the charset.</li>
 * <li>
 * <b>Choosing the content type</b><br/>
 * The content type is responsible for creating an actual Java Object from the
 * content of the resource. There are a few types provided but you can also pass
 * a custom {@link TextContentType}.</li>
 * <li>
 * <b>Choosing the bound type</b><br\>
 * This step serves two purposes. It specifies the type (or {@link Key}) that
 * will be bound by Guice. Additionally this type will be passed to the
 * implementation of {@link TextContentType#createInstance(Class, TextResource)}
 * .</li>
 * <li>
 * <b>Choosing the scope</b> <em>(Optional)</em><br\>
 * By default all types are bound unscoped. You may chose to specify an explicit
 * scope here.</li>
 * <li>
 * <b>Finalizing</b><br\>
 * Finishes the configuration by actually publishing a binding to the passed
 * {@link Binder}.</li>
 * </ol>
 *
 * @author Simon Taddiken
 */
public interface Resources {

    public static ChooseBufferType bind() {
        final ResourceUtil util = new ResourceUtil();
        final BeanUtil beanUtil = new BeanUtil();
        final TextResourceFactory factory = new TextResourceFactoryImpl(util);
        final ContentTypeFactory contentTypeFactory = new ContentTypeFactoryImpl(
                beanUtil);
        return new DSLImpl(factory, contentTypeFactory);
    }

    public static ChooseBufferType build() {
        return bind();
    }

    interface ChooseBufferType extends ChooseResources {
        ChooseResources buffered();

        ChooseResources constant();

        ChooseResources cached(CachingStrategy strategy);
    }

    interface ChooseResources {
        ChooseContentTypeAndCharset classPathResource(String path);

        ChooseContentTypeAndCharset classPathResource(String path, ClassLoader cl);

        ChooseContentTypeAndCharset servletResource(String path);

        ChooseContentTypeAndCharset fileResource(File file);

        ChooseContentTypeAndCharset pathResource(Path path);

        ChooseContentTypeAndCharset urlResource(URL url);

        ChooseContentTypeAndCharset urlResource(String url);

        ChooseContentType resource(TextResource resource);
    }

    interface ChooseContentTypeAndCharset extends ChooseContentType {
        ChooseContentType encodedWith(String charset);

        ChooseContentType encodedWith(Charset charset);

        ChooseContentType encodedWithSystemDefaultCharset();

        ChooseContentType encodedWithProvidedCharset();
    }

    interface ChooseContentType {
        ChooseTargetType containingText();

        ChooseTargetType containingJson();

        ChooseTargetType containingJson(GsonBuilder builder);

        ChooseTargetType containingXml();

        ChooseTargetType containingProperties();

        ChooseTargetType containing(TextContentType contentType);

        ChooseTargetType containing(Class<? extends TextContentType> contentTypeType);
    }

    interface ChooseTargetType {

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
