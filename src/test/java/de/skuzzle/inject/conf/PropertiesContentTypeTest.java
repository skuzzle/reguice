package de.skuzzle.inject.conf;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.ProvisionException;

@RunWith(MockitoJUnitRunner.class)
public class PropertiesContentTypeTest {

    public static interface FooBar {
        String getFoo();
    }

    @Mock
    private TextResource resource;

    private PropertiesContentType subject;

    @Before
    public void setUp() throws Exception {
        when(this.resource.openStream()).thenReturn(new StringReader("foo=bar"));

        this.subject = new PropertiesContentType();
    }

    @Test
    public void testCreateInstance() throws Exception {
        final FooBar inst = this.subject.createInstance(FooBar.class, this.resource);
        assertEquals("bar", inst.getFoo());
    }

    @Test(expected = ProvisionException.class)
    public void testIOException() throws Exception {
        when(this.resource.openStream()).thenThrow(IOException.class);
        this.subject.createInstance(FooBar.class, this.resource);
    }
}
