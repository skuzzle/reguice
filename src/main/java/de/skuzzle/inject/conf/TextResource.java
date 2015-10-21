package de.skuzzle.inject.conf;

import java.io.IOException;
import java.io.Reader;

public interface TextResource extends BinaryResource, Readable {

    Reader openStream() throws IOException;

}
