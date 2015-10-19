package de.skuzzle.inject.conf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NioResourceTest {

    @Mock
    private ResourceUtil util;
    @Mock
    private Path path;

    private final Charset charset = Charset.defaultCharset();

    private NioResource subject;


    @Before
    public void setUp() throws Exception {
        this.subject = new NioResource(this.util, this.path, this.charset);
    }

    @Test
    public void testOpenStream() throws Exception {
        final Reader reader = mock(Reader.class);
        final Charset charset = Charset.defaultCharset();
        when(this.util.newReader(this.path, charset)).thenReturn(reader);
        final Reader result = this.subject.openStream();
        assertSame(reader, result);
    }

    @Test
    public void testGetLastModified() throws Exception {
        when(this.util.getLastModifiedTime(this.path)).thenReturn(1337L);
        final long actual = this.subject.getLastModifiedTime();
        assertEquals(1337L, actual);
    }
}
