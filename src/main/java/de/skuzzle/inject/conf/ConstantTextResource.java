package de.skuzzle.inject.conf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

class ConstantTextResource implements TextResource {

    private final TextResource wrapped;
    private String bufferedString;
    private byte[] bufferedBytes;

    ConstantTextResource(TextResource wrapped) {
        this.wrapped = wrapped;
    }

    static TextResource wrap(TextResource resource) {
        return new ConstantTextResource(resource);
    }

    @Override
    public final synchronized InputStream openBinaryStream() throws IOException {
        if (this.bufferedBytes == null || rebufferBytes()) {
            try (InputStream stream = this.wrapped.openBinaryStream()) {
                this.bufferedBytes = ByteStreams.toByteArray(stream);
                newBytesBuffered();
            }
        }
        return new ByteArrayInputStream(this.bufferedBytes);
    }

    @Override
    public final synchronized Reader openStream() throws IOException {
        if (this.bufferedString == null || rebufferChars()) {
            try (Reader reader = this.wrapped.openStream()) {
                this.bufferedString = CharStreams.toString(reader);
                newCharsBuffered();
            }
        }
        return new StringReader(this.bufferedString);
    }

    protected void newBytesBuffered() throws IOException {
        // to be overridden by subclasses
    }

    protected void newCharsBuffered() throws IOException {
        // to be overridden by subclasses
    }

    protected boolean rebufferBytes() throws IOException {
        return false;
    }

    protected boolean rebufferChars() throws IOException {
        return false;
    }

    @Override
    public final long getLastModifiedTime() throws IOException {
        return this.wrapped.getLastModifiedTime();
    }
}
