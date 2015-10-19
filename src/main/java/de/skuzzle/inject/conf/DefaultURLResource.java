package de.skuzzle.inject.conf;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

class DefaultURLResource extends AbstractURLResource {

    private final URL url;

    DefaultURLResource(ResourceUtil util, Charset charset, URL url) {
        super(util, charset);
        this.url = url;
    }

    @Override
    protected URL getURL() throws IOException {
        return this.url;
    }

}
