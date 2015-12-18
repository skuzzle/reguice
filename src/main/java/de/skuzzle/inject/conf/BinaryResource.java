package de.skuzzle.inject.conf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a resource for which a binary stream can be obtained.
 *
 * @author Simon Taddiken
 */
public interface BinaryResource {

    /**
     * Opens a new binary connection to the actual resource. Its the callers
     * responsibility to take care of closing the returned stream. A typical
     * usage idiom is the usage of a try-resource block:
     *
     * <pre>
     * try (InputStream stream = resource.openBinaryStream()) {
     *     // ...
     * } catch (IOException e) {
     *     // ...
     * }
     * </pre>
     *
     * @return A new InputStream to read content of the resource.
     * @throws IOException If an IO error occurs.
     */
    InputStream openBinaryStream() throws IOException;

    /**
     * Writes the binary content of this resource to the specified output stream.
     *
     * @param out The target stream.
     * @return The number of bytes that have been copied.
     * @throws IOException If an IO error occurs.
     */
    long writeTo(OutputStream out) throws IOException;

    /**
     * Gets the modification date of this resource. Depending on the actual
     * implementation this method will return a best effort value for the last
     * modification date. It might not always be possible to obtain the date or
     * obtaining the date might result in an extra overhead of opening a
     * connection to the actual resource (like a web resource).
     *
     * @return The last modification date. A value of <code>0</code> indicates
     *         an unknown date.
     * @throws IOException If an IO error occurs while determining the last
     *             modification date.
     */
    long getLastModifiedTime() throws IOException;
}
