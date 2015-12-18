package de.skuzzle.inject.conf;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

import javax.inject.Provider;
import javax.servlet.ServletContext;

final class TextResourceFactoryImpl implements TextResourceFactory {

    private final ResourceUtil util;

    TextResourceFactoryImpl(ResourceUtil util) {
        this.util = util;
    }

    @Override
    public TextResource cache(TextResource resource, CachingStrategy strategy) {
        return new CachedTextResource(resource, strategy);
    }

    @Override
    public TextResource newClassPathResource(String path, ClassLoader cl,
            Charset charset) {
        return new ClassPathResource(this.util, cl, path, charset);
    }

    @Override
    public TextResource newServletResource(String path,
            Provider<ServletContext> servletContext, Charset charset) {
        return new WebContextResource(this.util, path, servletContext, charset);
    }

    @Override
    public TextResource newURLResource(URL url, Charset charset) {
        return new DefaultURLResource(this.util, charset, url);
    }

    @Override
    public TextResource newNioResource(Path path, Charset charset) {
        return new NioResource(this.util, path, charset);
    }

    @Override
    public TextResource newStringResource(String text) {
        return new StringTextResource(text, this.util);
    }
}
