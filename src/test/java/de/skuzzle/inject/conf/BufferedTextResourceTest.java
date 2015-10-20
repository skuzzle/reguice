package de.skuzzle.inject.conf;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

@RunWith(MockitoJUnitRunner.class)
public class BufferedTextResourceTest {

    @Mock
    private TextResource wrapped;


    private TextResource subject;

    @Before
    public void setUp() throws Exception {
        this.subject = BufferedTextResource.wrap(this.wrapped);
    }

    @Test
    public void testWrapAgain() throws Exception {
        assertSame(this.subject, BufferedTextResource.wrap(this.subject));
    }

    @Test
    public void testOpenStream() throws Exception {
        final String content = "foobar";
        final Reader reader = new StringReader(content);
        when(this.wrapped.openStream()).thenReturn(reader);

        final Reader r = this.subject.openStream();
        final String result = CharStreams.toString(r);
        assertEquals("foobar", result);
    }

    @Test
    public void testOpenStreamTwice() throws Exception {
        final Reader reader = new StringReader("foobar");
        when(this.wrapped.openStream()).thenReturn(reader);

        final Reader r1 = this.subject.openStream();
        final Reader r2 = this.subject.openStream();

        verify(this.wrapped, times(1)).openStream();
        assertEquals("foobar", CharStreams.toString(r1));
        assertEquals("foobar", CharStreams.toString(r2));
    }

    @Test
    public void testOpenStreamTwiceAfterModification() throws Exception {
        when(this.wrapped.getLastModifiedTime()).thenReturn(1337L);
        when(this.wrapped.openStream()).thenReturn(new StringReader("foobar1"));
        assertEquals("foobar1", CharStreams.toString(this.subject.openStream()));

        when(this.wrapped.getLastModifiedTime()).thenReturn(1338L);
        when(this.wrapped.openStream()).thenReturn(new StringReader("foobar2"));
        assertEquals("foobar2", CharStreams.toString(this.subject.openStream()));
    }

    @Test
    public void testOpenBinaryStream() throws Exception {
        final byte[] content = new byte[] { 1, 2, 3 };
        final InputStream stream = new ByteArrayInputStream(content);
        when(this.wrapped.openBinaryStream()).thenReturn(stream);

        final InputStream s = this.subject.openBinaryStream();
        final byte[] result = ByteStreams.toByteArray(s);
        assertArrayEquals(content, result);
    }

    @Test
    public void testOpenByteStreamTwice() throws Exception {
        final byte[] content = new byte[] { 1, 2, 3 };
        final InputStream stream = new ByteArrayInputStream(content);
        when(this.wrapped.openBinaryStream()).thenReturn(stream);

        final InputStream s1 = this.subject.openBinaryStream();
        final InputStream s2 = this.subject.openBinaryStream();

        verify(this.wrapped, times(1)).openBinaryStream();
        assertArrayEquals(content, ByteStreams.toByteArray(s1));
        assertArrayEquals(content, ByteStreams.toByteArray(s2));
    }

    @Test
    public void testOpenBinaryStreamTwiceAfterModification() throws Exception {
        final byte[] content1 = new byte[] { 1, 2, 3};
        final byte[] content2 = new byte[] { 3, 2, 1};

        when(this.wrapped.getLastModifiedTime()).thenReturn(1337L);
        when(this.wrapped.openBinaryStream()).thenReturn(new ByteArrayInputStream(content1));
        assertArrayEquals(content1, ByteStreams.toByteArray(this.subject.openBinaryStream()));

        when(this.wrapped.getLastModifiedTime()).thenReturn(1338L);
        when(this.wrapped.openBinaryStream()).thenReturn(new ByteArrayInputStream(content2));
        assertArrayEquals(content2, ByteStreams.toByteArray(this.subject.openBinaryStream()));
    }
}
