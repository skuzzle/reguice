package de.skuzzle.inject.conf;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.CharBuffer;
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
    public long writeTo(OutputStream out) throws IOException {
        checkArgument(out != null);
        return this.util.writeFromSource(this, out);
    }

    @Override
    public int read(CharBuffer cb) throws IOException {
        return this.util.readFromSource(this, cb);
    }

    @Override
    public long getLastModifiedTime() throws IOException {
        return this.util.getLastModifiedTime(this.path);
    }

    @Override
    public Reader openStream() throws IOException {
        return this.util.newReader(this.path, this.charset);
    }

    @Override
    public InputStream openBinaryStream() throws IOException {
        return this.util.newInputStream(this.path);
    }
}
