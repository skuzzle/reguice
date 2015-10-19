package de.skuzzle.inject.conf;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

abstract class AbstractURLResource implements TextResource {

    private final ResourceUtil util;
    private final Charset charset;

    AbstractURLResource(ResourceUtil util, Charset charset) {
        this.util = util;
        this.charset = charset;
    }

    protected abstract URL getURL() throws IOException;

    @Override
    public Reader openStream() throws IOException {
        if (this.charset == null) {
            return this.util.newReader(getURL());
        }
        return this.util.newReader(getURL(), this.charset);
    }

    @Override
    public long getLastModifiedTime() throws IOException {
        return this.util.getLastModifiedTime(getURL());
    }

}
