package de.skuzzle.inject.conf;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConstantTextResourceTest {

    @Mock
    private TextResource wrapped;

    private TextResource subject;

    @Before
    public void setUp() throws Exception {
        this.subject = ConstantTextResource.wrap(this.wrapped);
    }

    @Test
    public void testOpenStream() throws Exception {
        final String content = "foobar";
        final Reader reader = new StringReader(content);
        when(this.wrapped.openStream()).thenReturn(reader);

        final Reader r = this.subject.openStream();
        final String result = toString(r);
        assertEquals("foobar", result);
    }

    @Test
    public void testOpenStreamTwice() throws Exception {
        final String content = "foobar";
        final Reader reader = new StringReader(content);
        when(this.wrapped.openStream()).thenReturn(reader);

        final Reader r1 = this.subject.openStream();
        final Reader r2 = this.subject.openStream();

        verify(this.wrapped, times(1)).openStream();
        assertEquals("foobar", toString(r1));
        assertEquals("foobar", toString(r2));
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

    private String toString(Reader reader) throws IOException {
        final StringWriter writer = new StringWriter();
        final char[] buffer = new char[10];
        int len = 0;
        while ((len = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, len);
        }
        return writer.toString();
    }
}
