package de.skuzzle.inject.conf;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;

final class NioResource implements TextResource {

    private final Path path;
    private final ResourceUtil util;
    private final Charset charset;

    NioResource(ResourceUtil util, Path path, Charset charset) {
        this.util = util;
        this.path = path;
        this.charset = charset;
    }

    @Override
    public long getLastModifiedTime() throws IOException {
        return this.util.getLastModifiedTime(this.path);
    }

    @Override
    public Reader openStream() throws IOException {
        return this.util.newReader(this.path, this.charset);
    }

}
