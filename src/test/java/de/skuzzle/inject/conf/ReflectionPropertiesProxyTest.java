package de.skuzzle.inject.conf;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Proxy;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReflectionPropertiesProxyTest {

    public static interface TestInterface {
        int getTestInt();

        Integer getTestInteger();

        long getTestLong();

        Long getTestLong2();

        boolean isTestBool();

        Boolean isTestBoolean();

        byte getTestByte();

        Byte getTestByte2();

        short getTestShort();

        Short getTestShort2();

        float getTestFloat();

        Float getTestFloat2();

        double getTestDouble();

        Double getTestDouble2();

        String getTestString();

        Object getNoCoerce();

        int getWithArg(Object foo);
    }

    private final Properties props = new Properties();


    private TestInterface subject;

    @Before
    public void setUp() throws Exception {
        this.subject = (TestInterface) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] { TestInterface.class },
                new PropertiesProxy(this.props, new BeanUtil()));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetUnknown() throws Exception {
        this.subject.getTestByte();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithArg() throws Exception {
        this.subject.getWithArg(new Object());
    }

    @Test
    public void testGetNoCoerce() throws Exception {
        final Object obj = new Object();
        this.props.put("noCoerce", obj);
        assertEquals(obj, this.subject.getNoCoerce());
    }

    @Test
    public void getInts() throws Exception {
        this.props.put("testInt", "1");
        this.props.put("testInteger", "1");

        assertEquals(1, this.subject.getTestInt());
        assertEquals(Integer.valueOf(1), this.subject.getTestInteger());
    }

    @Test
    public void testGetLongs() throws Exception {
        this.props.put("testLong", "1");
        this.props.put("testLong2", "1");

        assertEquals(1, this.subject.getTestLong());
        assertEquals(Long.valueOf(1), this.subject.getTestLong2());
    }

    @Test
    public void testGetByte() throws Exception {
        this.props.put("testByte", "1");
        this.props.put("testByte2", "1");

        assertEquals((byte) 1, this.subject.getTestByte());
        assertEquals(Byte.valueOf((byte) 1), this.subject.getTestByte2());
    }

    @Test
    public void testGetShort() throws Exception {
        this.props.put("testShort", "1");
        this.props.put("testShort2", "1");

        assertEquals((short) 1, this.subject.getTestShort());
        assertEquals(Short.valueOf((short) 1), this.subject.getTestShort2());
    }

    @Test
    public void testGetBools() throws Exception {
        this.props.put("testBool", "true");
        this.props.put("testBoolean", "true");

        assertEquals(true, this.subject.isTestBool());
        assertEquals(Boolean.TRUE, this.subject.isTestBoolean());
    }

    @Test
    public void testGetFloat() throws Exception {
        this.props.put("testFloat", "1.2");
        this.props.put("testFloat2", "1.2");

        assertEquals(1.2f, this.subject.getTestFloat(), .01f);
        assertEquals(Float.valueOf(1.2f), this.subject.getTestFloat2());
    }

    @Test
    public void testGetDouble() throws Exception {
        this.props.put("testDouble", "1.2");
        this.props.put("testDouble2", "1.2");

        assertEquals(1.2, this.subject.getTestDouble(), .01);
        assertEquals(Double.valueOf(1.2), this.subject.getTestDouble2());
    }

    @Test
    public void testGetString() throws Exception {
        this.props.put("testString", "test");
        assertEquals("test", this.subject.getTestString());
    }
}
