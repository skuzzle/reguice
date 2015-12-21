package de.skuzzle.inject.conf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

class CachedTextResource implements TextResource {

    private final TextResource wrapped;
    private String bufferedString;
    private byte[] bufferedBytes;
    private final CachingStrategy cacheStrategy;
    private final ResourceUtil resourceUtil;

    CachedTextResource(TextResource wrapped, CachingStrategy strategy,
            ResourceUtil resourceUtil) {
        this.wrapped = wrapped;
        this.cacheStrategy = strategy;
        this.resourceUtil = resourceUtil;
    }

    @Override
    public int read(CharBuffer cb) throws IOException {
        return this.resourceUtil.readFromSource(this, cb);
    }

    @Override
    public long writeTo(OutputStream out) throws IOException {
        return this.resourceUtil.writeFromSource(this, out);
    }

    @Override
    public final synchronized InputStream openBinaryStream() throws IOException {
        if (this.bufferedBytes == null || rebufferBytes()) {
            try (InputStream stream = this.wrapped.openBinaryStream()) {
                this.bufferedBytes = ByteStreams.toByteArray(stream);
                this.cacheStrategy.binaryCacheRefreshed(this, this.bufferedBytes);
            }
        }
        return new ByteArrayInputStream(this.bufferedBytes);
    }

    @Override
    public final synchronized Reader openStream() throws IOException {
        if (this.bufferedString == null || rebufferChars()) {
            try (Reader reader = this.wrapped.openStream()) {
                this.bufferedString = CharStreams.toString(reader);
                this.cacheStrategy.textCacheRefreshed(this, this.bufferedString);
            }
        }
        return new StringReader(this.bufferedString);
    }

    protected final boolean rebufferBytes() throws IOException {
        return this.cacheStrategy.refreshBinaryCache(this);
    }

    protected final boolean rebufferChars() throws IOException {
        return this.cacheStrategy.refreshTextCache(this);
    }

    @Override
    public final long getLastModifiedTime() throws IOException {
        return this.wrapped.getLastModifiedTime();
    }
}
