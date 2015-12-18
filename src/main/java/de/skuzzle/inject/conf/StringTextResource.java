package de.skuzzle.inject.conf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

class StringTextResource implements TextResource {

    private final String s;
    private final long time;
    private final ResourceUtil util;

    StringTextResource(String s, ResourceUtil util) {
        this.s = s;
        this.util = util;
        this.time = System.currentTimeMillis();
    }

    @Override
    public InputStream openBinaryStream() throws IOException {
        return new ByteArrayInputStream(this.s.getBytes());
    }

    @Override
    public long writeTo(OutputStream out) throws IOException {
        return this.util.writeFromSource(this, out);
    }

    @Override
    public long getLastModifiedTime() throws IOException {
        return this.time;
    }

    @Override
    public int read(CharBuffer cb) throws IOException {
        return this.util.readFromSource(this, cb);
    }

    @Override
    public Reader openStream() throws IOException {
        return new StringReader(this.s);
    }

}
