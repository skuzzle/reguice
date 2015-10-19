package de.skuzzle.inject.conf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

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

    private Charset getCharset(URLConnection connection) {
        String cs = connection.getContentEncoding();
        if (cs == null) {
            cs = Charset.defaultCharset().name();
        }
        return Charset.forName(cs);
    }

    public Reader newReader(Path path, Charset cs) throws IOException {
        return Files.newBufferedReader(path, cs);
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

    public BufferedReader convert(Reader r) {
        if (r instanceof BufferedReader) {
            return (BufferedReader) r;
        }
        return new BufferedReader(r);
    }
}
