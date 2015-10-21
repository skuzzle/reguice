package de.skuzzle.inject.conf;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class BeanUtilTest {

    private final BeanUtil subject = new BeanUtil();

    @Before
    public void setUp() throws Exception {}

    @Test
    public void testGetMethodNameBoolean() throws Exception {
        assertEquals("cool", this.subject.getPropertyName("isCool"));
    }

    @Test
    public void testGetMethodNameDefault() throws Exception {
        assertEquals("fooBar", this.subject.getPropertyName("getFooBar"));
    }

    @Test
    public void testGetMemberNameNoBean() throws Exception {
        assertEquals("fooBar", this.subject.getPropertyName("fooBar"));
    }

    @Test
    public void testGetMemberNameGet() throws Exception {
        assertEquals("get", this.subject.getPropertyName("get"));
    }

    @Test
    public void testGetMemberNameIs() throws Exception {
        assertEquals("is", this.subject.getPropertyName("is"));
    }
}
