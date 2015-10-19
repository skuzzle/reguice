package de.skuzzle.inject.conf;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

class ConstantTextResource implements TextResource {

    private static final int BUFFER_SIZE = 4 * 1024;
    private final TextResource wrapped;
    private String buffer;

    ConstantTextResource(TextResource wrapped) {
        this.wrapped = wrapped;
    }

    static TextResource wrap(TextResource resource) {
        return new ConstantTextResource(resource);
    }

    @Override
    public synchronized Reader openStream() throws IOException {
        if (rebuffer()) {
            final StringWriter writer = new StringWriter();
            final char[] buffer = new char[BUFFER_SIZE];

            try (Reader reader = this.wrapped.openStream()) {
                int len = 0;
                while ((len = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, len);
                }
            }
            this.buffer = writer.toString();
        }
        return new StringReader(this.buffer);
    }

    protected boolean rebuffer() throws IOException {
        return this.buffer == null;
    }

    @Override
    public long getLastModifiedTime() throws IOException {
        return this.wrapped.getLastModifiedTime();
    }

}
