package de.skuzzle.inject.conf;

import static com.google.common.base.Preconditions.checkState;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.inject.Provider;
import javax.servlet.ServletContext;

final class WebContextResource extends AbstractURLResource {

    private final String resourcePath;
    private final Provider<ServletContext> servletContext;

    WebContextResource(ResourceUtil util, String resourcePath,
            Provider<ServletContext> servletContext, Charset charset) {
        super(util, charset);
        this.resourcePath = resourcePath;
        this.servletContext = servletContext;
    }

    @Override
    protected URL getURL() throws IOException {
        final ServletContext ctx = this.servletContext.get();
        checkState(ctx != null, "ServletContext not available");
        final URL url = ctx.getResource(this.resourcePath);
        if (url == null) {
            resourceNotFound();
        }
        return url;
    }

    private void resourceNotFound() throws FileNotFoundException {
        throw new FileNotFoundException(String.format(
                "Resource not found in servlet context: %s", this.resourcePath));
    }
}
