package de.skuzzle.inject.conf;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

@RunWith(MockitoJUnitRunner.class)
public class CachedTextResourceTest {

    @Mock
    private TextResource wrapped;

    private TextResource subject;

    @Before
    public void setUp() throws Exception {
        this.subject = CachedTextResource.wrap(this.wrapped,
                ConstantCacheStrategy.getInstance());
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
    public void testOpenStreamTwice() throws Exception {
        final String content = "foobar";
        final Reader reader = new StringReader(content);
        when(this.wrapped.openStream()).thenReturn(reader);

        final Reader r1 = this.subject.openStream();
        final Reader r2 = this.subject.openStream();

        verify(this.wrapped, times(1)).openStream();
        assertEquals("foobar", CharStreams.toString(r1));
        assertEquals("foobar", CharStreams.toString(r2));
    }

    @Test(expected = IOException.class)
    public void testCloseWrappedReader() throws Exception {
        final Reader r = mock(Reader.class);
        when(r.read()).thenReturn(-1);
        when(r.read(Mockito.any(char[].class))).thenReturn(-1);
        when(r.read(Mockito.any(CharBuffer.class))).thenReturn(-1);
        when(r.read(Mockito.any(char[].class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(-1);
        when(this.wrapped.openStream()).thenReturn(r);
        doThrow(IOException.class).when(r).close();

        this.subject.openStream();
    }

    @Test(expected = IOException.class)
    public void testCloseWrappedStream() throws Exception {
        final InputStream stream = mock(InputStream.class);
        when(stream.read()).thenReturn(-1);
        when(stream.read(Mockito.any(byte[].class))).thenReturn(-1);
        when(stream.read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(-1);
        when(this.wrapped.openBinaryStream()).thenReturn(stream);
        doThrow(IOException.class).when(stream).close();

        this.subject.openBinaryStream();
    }

    @Test(expected = IOException.class)
    public void testReaderException() throws Exception {
        when(this.wrapped.openStream()).thenThrow(IOException.class);
        this.subject.openStream();
    }

    @Test
    public void testGetLastModificationTime() throws Exception {
        when(this.wrapped.getLastModifiedTime()).thenReturn(1337L);
        assertEquals(1337L, this.subject.getLastModifiedTime());
    }
}
