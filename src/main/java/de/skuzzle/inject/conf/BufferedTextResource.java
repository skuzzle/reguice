package de.skuzzle.inject.conf;

import java.io.IOException;

final class BufferedTextResource extends ConstantTextResource {

    private long lastCharsRead;
    private long lastBytesRead;

    private BufferedTextResource(TextResource wrapped) {
        super(wrapped);
    }

    static TextResource wrap(TextResource resource) {
        if (resource instanceof BufferedTextResource) {
            return resource;
        }
        return new BufferedTextResource(resource);
    }

    @Override
    protected void newBytesBuffered() throws IOException {
        this.lastBytesRead = getLastModifiedTime();
    }

    @Override
    protected void newCharsBuffered() throws IOException {
        this.lastCharsRead = getLastModifiedTime();
    }

    @Override
    protected boolean rebufferBytes() throws IOException {
        return getLastModifiedTime() > this.lastBytesRead;
    }

    @Override
    protected boolean rebufferChars() throws IOException {
        return getLastModifiedTime() > this.lastCharsRead;
    }
}
