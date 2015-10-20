package de.skuzzle.inject.conf;

import java.io.IOException;
import java.io.InputStream;

public interface BinaryResource {

    InputStream openBinaryStream() throws IOException;

    long getLastModifiedTime() throws IOException;
}
