package de.skuzzle.inject.conf;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

import javax.servlet.ServletContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;

/**
 * Allows to bind resources from various sources with various content types. This class is
 * typically be used within a {@link Module} where you have access to a {@link Binder}
 * instance. It provides an <em>Embedded Domain Specific Language</em> (EDSL) for
 * configuring the binding. The starting point is the static method {@link #bind()}. The
 * last action for every resource to be bound is to call {@link Finalize#using(Binder)}
 * which will actually publish the configuration to the passed Binder.
 * <p>
 * The process of binding a resource consists of several steps that are implemented as
 * EDSL so it is nearly impossible to get it wrong. As mentioned above its mandatory to
 * always reach step 7 because otherwise your configuration is not applied to the Binder.
 * <b>There is no programmatic way of noticing a forgotten call to
 * {@link Finalize#using(Binder)}.</b>
 * <ol>
 * <li><b>Choosing a buffer type</b><em> (optional)</em><br>
 * You may chose between 4 different kinds of buffering the content of the resource to be
 * bound:
 * <ul>
 * <li><em>constant</em> - The resource will be read once and is then buffered in memory.
 * Further accesses to the resource will always return the in-memory view.</li>
 * <li><em>buffered</em> - Same as constant but the resource will be rebuffered if a
 * change of its {@link BinaryResource#getLastModifiedTime() modification date} is
 * detected.</li>
 * <li><em>reloadable</em> (default) - Every access to the resource will read it
 * again.</li>
 * <li><em>cached</em> - Specify a custom caching strategy.
 * </ul>
 * Depending on the scope (which is configured later, see step 6) not every buffer type
 * will make sense. If your resource is bound as {@link Singleton} there is no need to
 * buffer it, as it is anyway read only once.</li>
 * <li><b>Choosing the source</b><br>
 * During this mandatory step the source of the resource is specified. The resource can be
 * read from the class path, the file system, from an {@link URL} or from a
 * {@link ServletContext}. You can also directly provide an instance of
 * {@link TextResource} here.</li>
 * <li><b>Choosing the encoding</b> <em>(Optional)</em><br>
 * Reading characters from a binary source <em>always</em> requires an encoding. There a
 * several possibilities of specifying the encoding for the resource:
 * <ul>
 * <li><em>explicitly</em> - you pass the desired encoding either by name or as
 * {@link Charset} instance.</li>
 * <li><em>system</em> - the system default encoding as returned by
 * {@link Charset#defaultCharset()} will be used.</li>
 * <li><em>provided</em> (default) - Some resources come with embedded content types like
 * for example HTTP resources. Determining the content type might involve creating extra
 * requests/connections to the actual resource. If an encoding can not be determined the
 * implementation falls back to the system's default encoding.</li>
 * </ul>
 * Note that usage of a system's default encoding makes the behavior of your application
 * system dependent. Thus this should be avoided in favor of explicit specification of the
 * charset.</li>
 * <li><b>Choosing the content type</b><br>
 * The content type is responsible for creating an actual Java Object from the content of
 * the resource. There are a few types provided but you can also pass a custom
 * {@link TextContentType} or {@link TypedContentType}.</li>
 * <li><b>Choosing the bound type</b><br>
 * This step serves two purposes. It specifies the type (or {@link Key}) that will be
 * bound by Guice. Additionally this type will be passed to the implementation of
 * {@link TextContentType#createInstance(Class, TextResource)} .</li>
 * <li><b>Choosing the scope</b> <em>(Optional)</em><br>
 * By default all types are bound unscoped. You may chose to specify an explicit scope
 * here.</li>
 * <li><b>Finalizing</b><br>
 * Finishes the configuration by actually publishing a binding to the passed
 * {@link Binder}.</li>
 * </ol>
 *
 * @author Simon Taddiken
 */
public interface Resources {

    /**
     * Start of fluent interface chain for binding resources.
     *
     * @return Fluent interface object.
     */
    public static ChooseBufferType bind() {
        final ResourceUtil util = new ResourceUtil();
        final BeanUtil beanUtil = new BeanUtil();
        final TextResourceFactory factory = new TextResourceFactoryImpl(util);
        final ContentTypeFactory contentTypeFactory = new ContentTypeFactoryImpl(
                beanUtil);
        return new DSLImpl(factory, contentTypeFactory);
    }

    /**
     * Allows to choose the type of caching. This step is optional. If no caching strategy
     * is specified, the resource will not be cached at all and will be re-read every time
     * it is requested.
     *
     * @author Simon Taddiken
     */
    interface ChooseBufferType extends ChooseResources {

        /**
         * Caches the resource content once it has been read, but re-reads it when the
         * resource has changed since the last request. Change detection is based on
         * {@link TextResource#getLastModifiedTime()}.
         *
         * @return Fluent interface object.
         */
        ChooseResources changing();

        /**
         * Reads the resource content only once and caches it forever.
         *
         * @return Fluent interface object.
         */
        ChooseResources constant();

        /**
         * Uses the given strategy for caching resource contents.
         *
         * @param strategy The strategy to use.
         * @return Fluent interface object.
         */
        ChooseResources cached(CachingStrategy strategy);
    }

    /**
     * Allows to specify the resource location.
     *
     * @author Simon Taddiken
     */
    interface ChooseResources {
        /**
         * Specifies to load the resource from the given class path location. The resource
         * will be looked up using the current context class loader.
         *
         * @param path The resource path.
         * @return Fluent interface object.
         */
        ChooseContentTypeAndCharset classPathResource(String path);

        /**
         * Specifies to load the resource from the given class path location using the
         * given class loader.
         *
         * @param path The resource path.
         * @param cl The class loader to use for looking up the resource.
         * @return Fluent interface object.
         */
        ChooseContentTypeAndCharset classPathResource(String path, ClassLoader cl);

        /**
         * Specifies to load the resource relative to the current web application using
         * {@link ServletContext#getResource(String)}.
         *
         * @param path The resource path.
         * @return Fluent interface object.
         */
        ChooseContentTypeAndCharset servletResource(String path);

        /**
         * Specifies to load the resource from the given file system location.
         *
         * @param file The resource path.
         * @return Fluent interface object.
         */
        ChooseContentTypeAndCharset fileResource(File file);

        /**
         * Specifies to load the resource from the given file system location (NIO
         * version).
         *
         * @param path The resource path.
         * @return Fluent interface object.
         */
        ChooseContentTypeAndCharset pathResource(Path path);

        /**
         * Specifies to load the resource from given url.
         *
         * @param url The resource path.
         * @return Fluent interface object.
         */
        ChooseContentTypeAndCharset urlResource(URL url);

        /**
         * Specifies to load the resource from given url.
         *
         * @param url The resource path.
         * @return Fluent interface object.
         */
        ChooseContentTypeAndCharset urlResource(String url);

        /**
         * Explicitly binds the given {@link TextResource} implementation.
         *
         * @param resource The resource.
         * @return Fluent interface object.
         */
        ChooseContentType resource(TextResource resource);
    }

    /**
     * Allows to specify the charset of the resource to bind.
     *
     * @author Simon Taddiken
     */
    interface ChooseContentTypeAndCharset extends ChooseContentType {
        /**
         * Specifies the charset as string.
         *
         * @param charset The charset name.
         * @return Fluent interface object.
         */
        ChooseContentType encodedWith(String charset);

        /**
         * Specifies the charset.
         *
         * @param charset The charset.
         * @return Fluent interface object.
         */
        ChooseContentType encodedWith(Charset charset);

        /**
         * Specifies to use the system's default charset (as determined by
         * {@link Charset#defaultCharset()}).
         *
         * @return Fluent interface object.
         */
        ChooseContentType encodedWithSystemDefaultCharset();

        /**
         * Will try to obtain the charset from the resource itself (i.e. by inspecting
         * header information of URL based resources). Should only be used if you are sure
         * that such information is availabel. If no charset can be determined from the
         * resource, this method falls back to {@link Charset#defaultCharset()}.
         *
         * @return Fluent interface object.
         */
        ChooseContentType encodedWithProvidedCharset();
    }

    /**
     * Allows to specify the content type of the resource to bind.
     *
     * @author Simon Taddiken
     */
    interface ChooseContentType {
        /**
         * Specifies that the resource content should be made available as a String.
         * Implies that the type to bind to must be <code>String.class</code>. This is not
         * compiler checked and thus raises a runtime exception when binding to a
         * different type.
         *
         * @return Fluent interface object.
         */
        TypeAlreadyChosen<String> containingText();

        /**
         * Specifies that the resource content should be parsed as json object. If the
         * type to which the resource is bound is an interface, the framework
         * automatically creates a proxy implementation which delegates all bean styled
         * getter methods to the respective json property.
         *
         * @return Fluent interface object.
         */
        ChooseTargetType containingJson();

        /**
         * Specifies that the resource content should be parsed as json object. If the
         * type to which the resource is bound is an interface, the framework
         * automatically creates a proxy implementation which delegates all bean style
         * getter methods to the respective json property.
         *
         * @param builder Builder for creating the {@link Gson} object to use for
         *            deserializing.
         * @return Fluent interface object.
         */
        ChooseTargetType containingJson(GsonBuilder builder);

        /**
         * Specifies that the resource content should be parsed as a java properties file.
         * If the type to which the resource is bound is an interface, the framework
         * automatically creates a proxy implementation which delegates all bean style
         * getter methods to the respective property.
         *
         * @return Fluent interface object.
         */
        ChooseTargetType containingProperties();

        /**
         * Specifies that the resource content will be interpreted by the given
         * {@link TextContentType} implementation.
         *
         * @param contentType The content type.
         * @return Fluent interface object.
         */
        ChooseTargetType containing(TextContentType contentType);

        /**
         * Specifies that the resource content will be interpreted by the given
         * {@link TextContentType} implementation.
         *
         * @param contentType The content type.
         * @return Fluent interface object.
         */
        <T> TypeAlreadyChosen<T> containing(TypedContentType<T> contentType);

        /**
         * Specifies that the resource content will be interpreted by the given
         * {@link TextContentType} implementation. The implementation will be looked up
         * with the injector using the given Class as key.
         *
         * @param contentType The content type.
         * @return Fluent interface object.
         */
        ChooseTargetType containing(Class<? extends TextContentType> contentTypeType);
    }

    /**
     * Allows to specify the annotation part of {@link TextContentType} bindings for which
     * the type they produce is already known.
     *
     * @author Simon Taddiken
     */
    interface TypeAlreadyChosen<T> extends FinalizeWithScope<T> {
        /**
         * Gives the key a name.
         *
         * @param name The name.
         * @return Fluent interface object.
         */
        FinalizeWithScope<T> named(String name);

        /**
         * Gives the key the given annotation type.
         *
         * @param annotationType The annotation type.
         * @return Fluent interface object.
         */
        FinalizeWithScope<T> annotatedWith(Class<? extends Annotation> annotationType);

        /**
         * Gives the key the given annotation.
         *
         * @param annotation The annotation.
         * @return
         */
        FinalizeWithScope<T> annotatedWith(Annotation annotation);
    }

    /**
     * Allows to specify the key to bind the {@link TextContentType} to.
     *
     * @author Simon Taddiken
     */
    interface ChooseTargetType {

        /**
         * Binds to the given key.
         *
         * @param key The key.
         * @return Fluent interface object.
         */
        <T> FinalizeWithScope<T> to(Key<T> key);

        /**
         * Binds to the given class and allows to further refine the key using the
         * resulting fluent interface object.
         *
         * @param type The type to bind to.
         * @return Fluent interface object.
         */
        <T> TypeAlreadyChosen<T> to(Class<T> type);

    }

    /**
     * Allows to specify the scope to bind in.
     *
     * @author Simon Taddiken
     */
    interface FinalizeWithScope<T> extends Finalize<T> {

        /**
         * The scope to bind the content in.
         *
         * @param scope The scope.
         * @return
         */
        Finalize<T> in(Class<? extends Annotation> scope);
    }

    /**
     * Finalizes the configuration.
     *
     * @author Simon Taddiken
     */
    interface Finalize<T> {

        /**
         * Publishes the fluent configuration to the {@link Binder}.
         *
         * @param binder The binder.
         */
        void using(Binder binder);
    }
}
