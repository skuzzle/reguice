package de.skuzzle.inject.conf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

final class ClassPathResource extends AbstractURLResource {

    private final String resourcePath;
    private final ClassLoader classLoader;

    ClassPathResource(ResourceUtil util, ClassLoader classLoader, String resourcePath,
            Charset charset) {
        super(util, charset);
        this.classLoader = classLoader;
        this.resourcePath = resourcePath;
    }

    @Override
    protected URL getURL() throws IOException {
        final URL url = this.classLoader.getResource(this.resourcePath);
        if (url == null) {
            resourceNotFound();
        }
        return url;
    }

    private void resourceNotFound() throws FileNotFoundException {
        throw new FileNotFoundException(String.format(
                "Resource not found using Classloader '%s' : %s",
                this.classLoader.toString(), this.resourcePath));
    }
}
