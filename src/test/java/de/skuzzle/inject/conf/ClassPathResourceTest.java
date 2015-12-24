package de.skuzzle.inject.conf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClassPathResourceTest {

    @Mock
    private ClassLoader classLoader;
    @Mock
    private ResourceUtil util;

    private final String resourcePath = "foo/bar";

    private ClassPathResource subject;

    @Before
    public void setUp() throws Exception {
        this.subject = new ClassPathResource(this.util, this.classLoader,
                this.resourcePath, Charset.defaultCharset());
    }

    @Test
    public void testOpenStream() throws Exception {
        final Reader reader= mock(Reader.class);
        final URL url = new URL("http://www.google.com");
        final Charset charset = Charset.defaultCharset();
        when(this.classLoader.getResource(this.resourcePath)).thenReturn(url);
        when(this.util.newReader(Mockito.any(InputStream.class),
                Mockito.eq(charset))).thenReturn(reader);

        final Reader actual = this.subject.openStream();
        assertSame(reader, actual);
    }

    @Test
    public void testOpenBinaryStream() throws Exception {
        final InputStream in = mock(InputStream.class);

        final URL url = new URL("http://www.google.com");
        when(this.classLoader.getResource(this.resourcePath)).thenReturn(url);
        when(this.util.newInputStream(url)).thenReturn(in);
        final InputStream actual = this.subject.openBinaryStream();
        assertSame(in, actual);
    }

    @Test
    public void testNoCharset() throws Exception {
        this.subject = new ClassPathResource(this.util, this.classLoader,
                this.resourcePath, null);
        final Reader reader= mock(Reader.class);
        final URL url = new URL("http://www.google.com");
        when(this.classLoader.getResource(this.resourcePath)).thenReturn(url);
        when(this.util.newReader(url)).thenReturn(reader);

        final Reader actual = this.subject.openStream();
        assertSame(reader, actual);
    }

    @Test
    public void testGetLastModified() throws Exception {
        final URL url = new URL("http://www.google.com");
        when(this.classLoader.getResource(this.resourcePath)).thenReturn(url);
        when(this.util.getLastModifiedTime(url)).thenReturn(1337L);
        final long actual = this.subject.getLastModifiedTime();
        assertEquals(1337L, actual);
    }

    @Test(expected = FileNotFoundException.class)
    public void testNotFound() throws Exception {
        this.subject.openStream();
    }

}
