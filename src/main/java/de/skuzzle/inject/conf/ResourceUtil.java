package de.skuzzle.inject.conf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.io.ByteStreams;

class ResourceUtil {

    public long getLastModifiedTime(URL url) throws IOException {
        final URLConnection con = url.openConnection();
        if (con instanceof JarURLConnection) {
            return ((JarURLConnection) con).getJarEntry().getTime();
        } else {
            return con.getLastModified();
        }
    }

    public long getLastModifiedTime(Path path) throws IOException {
        return Files.getLastModifiedTime(path).toMillis();
    }

    private static Charset getCharset(URLConnection connection) {
        final String cs = connection.getContentEncoding();
        if (cs == null) {
            return Charset.defaultCharset();
        }
        return Charset.forName(cs);
    }

    public Reader newReader(Path path, Charset cs) throws IOException {
        if (cs == null) {
            return Files.newBufferedReader(path);
        } else {
            return Files.newBufferedReader(path, cs);
        }
    }

    public InputStream newInputStream(URL url) throws IOException {
        return url.openStream();
    }

    public InputStream newInputStream(Path path) throws IOException {
        return Files.newInputStream(path);
    }

    public Reader newReader(URL url) throws IOException {
        final URLConnection connection = url.openConnection();
        connection.connect();
        final Charset cs = getCharset(connection);
        return newReader(connection.getInputStream(), cs);
    }

    public Reader newReader(URL url, Charset charset) throws IOException {
        return newReader(url.openStream(), charset);
    }

    public Reader newReader(InputStream stream, Charset charset) throws IOException {
        return new InputStreamReader(stream, charset);
    }

    public int readFromSource(TextResource resource, CharBuffer buffer)
            throws IOException {
        try (Reader reader = resource.openStream()) {
            return reader.read(buffer);
        }
    }

    public long writeFromSource(BinaryResource resource, OutputStream target)
            throws IOException {
        try (InputStream in = resource.openBinaryStream()) {
            return ByteStreams.copy(in, target);
        }
    }
}
