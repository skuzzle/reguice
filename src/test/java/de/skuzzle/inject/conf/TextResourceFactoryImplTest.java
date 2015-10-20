package de.skuzzle.inject.conf;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

import javax.inject.Provider;
import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TextResourceFactoryImplTest {

    @Mock
    private ResourceUtil util;

    private TextResourceFactoryImpl subject;

    @Before
    public void setUp() throws Exception {
        this.subject = new TextResourceFactoryImpl(this.util);
    }

    @Test
    public void testNewClassPathResource() throws Exception {
        final ClassLoader cl = mock(ClassLoader.class);
        final TextResource resource = this.subject.newClassPathResource("foobar", cl,
                Charset.defaultCharset());
        assertTrue(resource instanceof ClassPathResource);
    }

    @Test
    public void testNewServletResource() throws Exception {
        final Provider<ServletContext> provider = mock(Provider.class);
        final TextResource resource = this.subject.newServletResource("foobar", provider,
                Charset.defaultCharset());
        assertTrue(resource instanceof WebContextResource);
    }

    @Test
    public void testNewUrlResource() throws Exception {
        final URL url = new URL("http://www.google.com");
        final TextResource resource = this.subject.newURLResource(url,
                Charset.defaultCharset());
        assertTrue(resource instanceof DefaultURLResource);
    }

    @Test
    public void testNewNioResource() throws Exception {
        final Path path = mock(Path.class);
        final TextResource resource = this.subject.newNioResource(path,
                Charset.defaultCharset());
        assertTrue(resource instanceof NioResource);
    }
}
