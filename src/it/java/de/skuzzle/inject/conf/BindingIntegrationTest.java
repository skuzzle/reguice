package de.skuzzle.inject.conf;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
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
                        }});
                } catch (final MalformedURLException e) {
                    e.printStackTrace();
                }

                bind(ServletContext.class).toInstance(mockCtx);

                Resources.bind().constant()
                        .servletResource("test.properties")
                        .containingText()
                        .to(String.class, "fromServlet")
                        .using(binder());

                Resources.bind().buffered()
                        .classPathResource("test.json")
                        .containingJson()
                        .to(JsonInterface.class)
                        .using(binder());

                Resources.bind().buffered()
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
    public void testPlainText() throws Exception {
        assertEquals("just a text file\n:D", this.textContent);
    }
}
