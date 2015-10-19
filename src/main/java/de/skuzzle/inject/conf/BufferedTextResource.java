package de.skuzzle.inject.conf;

import java.io.IOException;

final class BufferedTextResource extends ConstantTextResource {

    private long lastRead;

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
    protected boolean rebuffer() throws IOException {
        if (super.rebuffer()) {
            this.lastRead = getLastModifiedTime();
            return true;
        }
        final long lastMod = getLastModifiedTime();
        if (lastMod > this.lastRead) {
            this.lastRead = lastMod;
            return true;
        }
        return false;
    }
}
