package de.skuzzle.inject.conf;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.GsonBuilder;
import com.google.inject.ProvisionException;

@RunWith(MockitoJUnitRunner.class)
public class JsonContentTypeTest {

    private static enum TestEnum {
        FOO, Bar
    }

    private static class Sample {
        public int foo = 1;
    }

    private static interface Sample2 {
        int getFoo();

        double getPi();

        String getBar();

        boolean isCool();

        int[] getArray();

        Sample3 getSample();

        Object getUnknown();

        long getWithParameter(Object object);

        List<String> getStringList();

        TestEnum getEnum();
    }

    private static interface Sample3 {
        Object getObject();
    }

    private final String json = "{ foo: 1 }";

    private final String json2 = "{" +
            "  foo: 1337,\n" +
            "  pi: 3.1415,\n" +
            "  bar: 'xyz',\n" +
            "  cool: true,\n" +
            "  array: [1, 2, 3],\n" +
            "  sample: {\n" +
            "    object: 'abc'\n" +
            "  }," +
            "  stringList: [\n" +
            "    'foo', 'bar'\n" +
            "  ],\n" +
            "  enum: 'Bar'\n" +
            "}";

    @Mock
    private TextResource resource;

    private final JsonContentType subject = new JsonContentType(new BeanUtil(),
            new GsonBuilder());

    @Test
    public void testCreateInstance() throws Exception {
        when(this.resource.openStream()).thenReturn(new StringReader(this.json));
        final Sample sample = this.subject.createInstance(Sample.class, this.resource);

        assertEquals(1, sample.foo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithParameter() throws Exception {
        final Reader reader = new StringReader(this.json2);
        when(this.resource.openStream()).thenReturn(reader);
        final Sample2 inst = this.subject.createInstance(Sample2.class, this.resource);
        inst.getWithParameter(new Object());
    }

    @Test
    public void testCreateInterface() throws Exception {
        final Reader reader = new StringReader(this.json2);
        when(this.resource.openStream()).thenReturn(reader);
        final Sample2 inst = this.subject.createInstance(Sample2.class, this.resource);
        assertEquals(1337, inst.getFoo());
        assertEquals(3.1415, inst.getPi(), 0.1);
        assertEquals("xyz", inst.getBar());
        assertEquals(true, inst.isCool());
        assertEquals(null, inst.getUnknown());
        assertEquals("abc", inst.getSample().getObject());
        assertArrayEquals(new int[] { 1, 2, 3 }, inst.getArray());
    }

    @Test(expected = ProvisionException.class)
    public void testIOException() throws Exception {
        when(this.resource.openStream()).thenThrow(IOException.class);
        this.subject.createInstance(Sample.class, this.resource);
    }

    @Test
    public void testStringList() throws Exception {
        final Reader reader = new StringReader(this.json2);
        when(this.resource.openStream()).thenReturn(reader);
        final Sample2 inst = this.subject.createInstance(Sample2.class, this.resource);

        final List<String> expected = Arrays.asList("foo", "bar");
        assertEquals(expected, inst.getStringList());
    }

    @Test
    public void testEnum() throws Exception {
        final Reader reader = new StringReader(this.json2);
        when(this.resource.openStream()).thenReturn(reader);
        final Sample2 inst = this.subject.createInstance(Sample2.class, this.resource);

        assertEquals(TestEnum.Bar, inst.getEnum());
    }
}
