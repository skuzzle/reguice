package de.skuzzle.inject.conf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultURLResourceTest {

    @Mock
    private ResourceUtil util;

    private DefaultURLResource subject;

    private URL url;

    @Before
    public void setUp() throws Exception {
        this.url = new URL("http://www.google.com");
        this.subject = new DefaultURLResource(this.util, Charset.defaultCharset(),
                this.url);
    }

    @Test
    public void testOpenStream() throws Exception {
        final Reader reader = mock(Reader.class);
        when(this.util.newReader(this.url, Charset.defaultCharset())).thenReturn(reader);
        final Reader actual = this.subject.openStream();
        assertSame(reader, actual);
    }

    @Test
    public void testNoCharset() throws Exception {
        this.subject = new DefaultURLResource(this.util, null, this.url);

        final Reader reader = mock(Reader.class);
        when(this.util.newReader(this.url)).thenReturn(reader);
        final Reader actual = this.subject.openStream();
        assertSame(reader, actual);
    }

    @Test
    public void testGetLastModified() throws Exception {
        when(this.util.getLastModifiedTime(this.url)).thenReturn(1337L);
        assertEquals(1337L, this.subject.getLastModifiedTime());
    }
}
