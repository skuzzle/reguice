package de.skuzzle.inject.conf;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.ProvisionException;

@RunWith(MockitoJUnitRunner.class)
public class JsonContentTypeTest {

    private static class Sample {
        public int foo = 1;
    }

    private final String json = "{ foo: 1 }";

    @Mock
    private TextResource resource;

    private final JsonContentType subject = new JsonContentType();

    @Test
    public void testCreateInstance() throws Exception {
        when(this.resource.openStream()).thenReturn(new StringReader(this.json));
        final Sample sample = this.subject.createInstance(Sample.class, this.resource);

        assertEquals(1, sample.foo);
    }

    @Test(expected = ProvisionException.class)
    public void testIOException() throws Exception {
        when(this.resource.openStream()).thenThrow(IOException.class);
        this.subject.createInstance(Sample.class, this.resource);
    }
}
