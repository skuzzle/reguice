package de.skuzzle.inject.conf;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

import javax.inject.Provider;
import javax.servlet.ServletContext;

/**
 * Internal interface for instantiating various kinds of {@link TextResource}.
 *
 * @author Simon Taddiken
 */
interface TextResourceFactory {

    TextResource newClassPathResource(String path, ClassLoader cl, Charset charset);

    TextResource newServletResource(String path, Provider<ServletContext> servletContext,
            Charset charset);

    TextResource newURLResource(URL url, Charset charset);

    TextResource newNioResource(Path path, Charset charset);
}
