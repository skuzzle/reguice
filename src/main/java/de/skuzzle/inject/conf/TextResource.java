package de.skuzzle.inject.conf;

import java.io.IOException;
import java.io.Reader;

public interface TextResource {

    public Reader openStream() throws IOException;

    public long getLastModifiedTime() throws IOException;
}
