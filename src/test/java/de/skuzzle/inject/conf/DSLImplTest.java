package de.skuzzle.inject.conf;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

@RunWith(MockitoJUnitRunner.class)
public class DSLImplTest {

    @Mock
    private TextResourceFactory factory;
    @Mock
    private Binder binder;
    @Mock
    private LinkedBindingBuilder<DSLImplTest> linkedBuilder;
    @Mock
    private ScopedBindingBuilder scopedBuilder;


    private ContentTypeFactoryImpl contentTypeFactory;

    private final Key<DSLImplTest> selfType = Key.get(DSLImplTest.class);
    private DSLImpl subject;

    @Before
    public void setUp() throws Exception {
        this.contentTypeFactory = new ContentTypeFactoryImpl(new BeanUtil());
        this.subject = new DSLImpl(this.factory, this.contentTypeFactory);

        when(this.binder.bind(this.selfType)).thenReturn(this.linkedBuilder);
        when(this.linkedBuilder.toProvider(Mockito.any(Provider.class))).thenReturn(this.scopedBuilder);
    }

    @Test
    public void testConfigureClassPathResource() throws Exception {
        final ClassLoader clMock = mock(ClassLoader.class);
        final TextContentType contentType = mock(TextContentType.class);

        when(contentType.createInstance(Mockito.eq(DSLImplTest.class),
                Mockito.isA(BufferedTextResource.class))).thenReturn(this);

        this.subject.buffered()
                .classPathResource("foo/bar", clMock)
                .containing(contentType)
                .to(getClass())
                .in(Singleton.class)
                .using(this.binder);

        verify(this.scopedBuilder).in(Singleton.class);
        verify(this.binder).bind(this.selfType);

        final ArgumentCaptor<Provider> captor = ArgumentCaptor.forClass(Provider.class);
        verify(this.linkedBuilder).toProvider(captor.capture());
        verify(this.factory).newClassPathResource("foo/bar", clMock, null);

        final Provider prov = captor.getValue();
        assertSame(this, prov.get());
    }

    @Test
    public void testBindClassPathResourceContextCL() throws Exception {
        this.subject.reloadable()
                .classPathResource("foo/bar")
                .encodedWith("UTF-8")
                .containingProperties()
                .to(getClass())
                .using(this.binder);

        verify(this.factory).newClassPathResource("foo/bar",
                Thread.currentThread().getContextClassLoader(), Charset.forName("UTF-8"));
    }

    @Test
    public void testConfigureServletResource() throws Exception {
        final TextContentType contentType = mock(TextContentType.class);

        when(contentType.createInstance(Mockito.eq(DSLImplTest.class),
                Mockito.isA(ConstantTextResource.class))).thenReturn(this);

        this.subject.constant()
                .servletResource("foo/bar")
                .encodedWith(StandardCharsets.UTF_8)
                .containing(contentType)
                .to(getClass())
                .using(this.binder);

        verify(this.scopedBuilder, never()).in(any(Class.class));
        verify(this.binder).bind(this.selfType);

        final ArgumentCaptor<Provider> captor = ArgumentCaptor.forClass(Provider.class);
        verify(this.linkedBuilder).toProvider(captor.capture());
        verify(this.factory).newServletResource(eq("foo/bar"), any(Provider.class),
                eq(StandardCharsets.UTF_8));

        final Provider prov = captor.getValue();
        assertSame(this, prov.get());
    }

    @Test
    public void testBindCustomResource() throws Exception {
        final TextContentType contentType = mock(TextContentType.class);
        final TextResource resource = mock(TextResource.class);

        when(contentType.createInstance(DSLImplTest.class, resource)).thenReturn(this);

        this.subject.reloadable()
                .resource(resource)
                .containing(contentType)
                .to(getClass())
                .using(this.binder);

        final ArgumentCaptor<Provider> captor = ArgumentCaptor.forClass(Provider.class);
        verify(this.linkedBuilder).toProvider(captor.capture());

        final Provider prov = captor.getValue();
        assertSame(this, prov.get());
    }

    @Test
    public void testBindNioResource() throws Exception {
        final Path path = mock(Path.class);
        this.subject.reloadable()
                .pathResource(path)
                .encodedWithSystemDefaultCharset()
                .containingJson()
                .to(getClass())
                .using(this.binder);

        verify(this.factory).newNioResource(path, Charset.defaultCharset());
    }

    @Test
    public void testBindFileResource() throws Exception {
        this.subject.fileResource(new File("."))
                .containingJson()
                .to(Key.get(getClass()))
                .using(this.binder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBindInvalidURL() throws Exception {
        this.subject.buffered()
                .urlResource("sdfsdfs<t");
    }

    @Test
    public void testBindURL() throws Exception {
        this.subject.reloadable()
                .urlResource("http://www.google.de")
                .containingJson()
                .to(getClass())
                .using(this.binder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest1() throws Exception {
        this.subject.classPathResource(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest2() throws Exception {
        this.subject.classPathResource("foo", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest3() throws Exception {
        this.subject.classPathResource(null, mock(ClassLoader.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest4() throws Exception {
        this.subject.fileResource(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest5() throws Exception {
        this.subject.pathResource(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest6() throws Exception {
        this.subject.urlResource((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest7() throws Exception {
        this.subject.urlResource((URL) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest8() throws Exception {
        this.subject.containing(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest9() throws Exception {
        this.subject.encodedWith((Charset) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest10() throws Exception {
        this.subject.encodedWith((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest11() throws Exception {
        this.subject.to((Class) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest12() throws Exception {
        this.subject.to((Key) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest13() throws Exception {
        this.subject.to(getClass()).using(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTest14() throws Exception {
        this.subject.to(getClass()).in(null);
    }
}
