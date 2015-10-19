package de.skuzzle.inject.conf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import javax.inject.Provider;
import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WebContextResourceTest {

    @Mock
    private ServletContext ctx;
    @Mock
    private Provider<ServletContext> provider;
    @Mock
    private ResourceUtil util;

    private String resourcePath;
    private WebContextResource subject;

    @Before
    public void setUp() throws Exception {
        when(this.provider.get()).thenReturn(this.ctx);
        this.resourcePath = "foo/bar";
        this.subject = new WebContextResource(
                this.util,
                this.resourcePath,
                this.provider,
                Charset.defaultCharset());
    }

    @Test(expected = FileNotFoundException.class)
    public void testOpenResourceNotFound() throws Exception {
        this.subject.openStream();
    }

    @Test
    public void testGetLastModified() throws Exception {
        final URL url = new URL("http://www.google.com");
        when(this.ctx.getResource(this.resourcePath)).thenReturn(url);
        when(this.util.getLastModifiedTime(url)).thenReturn(1337L);
        final long actual = this.subject.getLastModifiedTime();
        assertEquals(1337L, actual);
    }

    @Test
    public void testOpenStream() throws Exception {
        final Reader reader = mock(Reader.class);
        final URL url = new URL("http://www.google.com");
        when(this.ctx.getResource(this.resourcePath)).thenReturn(url);
        when(this.util.newReader(url, Charset.defaultCharset())).thenReturn(reader);
        final Reader result = this.subject.openStream();
        assertSame(reader, result);
    }

    @Test
    public void testOpenStreamNoCharset() throws Exception {
        this.subject = new WebContextResource(this.util, this.resourcePath,
                this.provider, null);

        final Reader reader = mock(Reader.class);
        final URL url = new URL("http://www.google.com");
        when(this.ctx.getResource(this.resourcePath)).thenReturn(url);
        when(this.util.newReader(url)).thenReturn(reader);
        final Reader result = this.subject.openStream();
        assertSame(reader, result);
    }
}
