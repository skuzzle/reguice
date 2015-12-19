package de.skuzzle.inject.conf;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;

public class BindingIntegrationTest {

    @Inject
    private JsonInterface jsonContent;
    @Inject
    private String textContent;
    @Inject
    private PropertiesInterface propertiesContent;
    @Inject
    private Properties props;
    @Inject
    @Named("fromServlet")
    private String fromServlet;
    @Inject
    private Object object;

    private static final Object TEST_OBJECT = new Object();

    public static class TestTextContentType implements TextContentType {

        @Override
        public <T> T createInstance(Class<T> type, TextResource resource) {
            return type.cast(TEST_OBJECT);
        }

    }

    @Before
    public void setup() {
        Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                final ServletContext mockCtx = mock(ServletContext.class);
                try {
                    when(mockCtx.getResource(Mockito.anyString())).thenAnswer(new Answer<URL>() {

                        @Override
                        public URL answer(InvocationOnMock invocation) throws Throwable {
                            final String s = invocation.getArgumentAt(0, String.class);
                            return getClass().getClassLoader().getResource(s);
                        }
                    });
                } catch (final MalformedURLException e) {
                    e.printStackTrace();
                }

                bind(ServletContext.class).toInstance(mockCtx);

                try {
                    final File file = new File(getClass().getResource(
                            "/test.properties").toURI());
                    Resources.bind()
                            .fileResource(file)
                            .containing(TestTextContentType.class)
                            .to(Object.class)
                            .using(binder());
                } catch (final URISyntaxException e) {
                    e.printStackTrace();
                }

                Resources.bind().constant()
                        .servletResource("test.properties")
                        .containingText()
                        .to(String.class, "fromServlet")
                        .in(Singleton.class)
                        .using(binder());

                Resources.bind().buffered()
                        .classPathResource("test.json")
                        .containingJson()
                        .to(JsonInterface.class)
                        .using(binder());

                Resources.bind().cached(new TestCachingStrategy())
                        .classPathResource("test.txt")
                        .containingText()
                        .to(String.class)
                        .using(binder());

                Resources.bind().buffered()
                        .classPathResource("test.properties")
                        .containingProperties()
                        .to(Properties.class)
                        .using(binder());

                Resources.bind().buffered()
                        .classPathResource("test.properties")
                        .containingProperties()
                        .to(PropertiesInterface.class)
                        .using(binder());

            }
        }).injectMembers(this);
    }

    @Test
    public void testCustomTextContentType() throws Exception {
        assertEquals(TEST_OBJECT, this.object);
    }

    @Test
    public void testArrayFromJson() throws Exception {
        assertArrayEquals(new int[] { 1, 2, 3 }, this.jsonContent.getArray());
    }

    @Test
    public void testInterfaceObjectFromJson() throws Exception {
        assertEquals("abc", this.jsonContent.getSample().getObject());
    }

    @Test
    public void testPlainText() throws Exception {
        assertEquals("just a text file\n:D", this.textContent);
    }

    @Test
    public void testConvertedObjectFromJson() throws Exception {
        assertArrayEquals(new String[] { "a", "b", "c" },
                this.jsonContent.getSampleObject().getContent());
    }

    @Test
    public void testConvertedObjectFromProperties() throws Exception {
        assertArrayEquals(new String[] { "a", "b", "c" },
                this.propertiesContent.getSampleObject().getContent());
    }
}
