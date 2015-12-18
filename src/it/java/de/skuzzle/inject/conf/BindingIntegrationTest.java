package de.skuzzle.inject.conf;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setup() {
        Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {

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
