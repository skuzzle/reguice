package de.skuzzle.inject.conf;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.nio.CharBuffer;

import org.junit.Before;
import org.junit.Test;

public class ResourceUtilTest {

    ResourceUtil subject;

    @Before
    public void setUp() throws Exception {
        this.subject = new ResourceUtil();
    }

    @Test
    public void testReadToBuffer() throws Exception {
        final TextResource resource = mock(TextResource.class);
        final CharBuffer buffer = mock(CharBuffer.class);
        final Reader reader = mock(Reader.class);
        when(resource.openStream()).thenReturn(reader);
        when(reader.read(buffer)).thenReturn(1337);
        assertEquals(1337,  this.subject.readFromSource(resource, buffer));
    }

}
