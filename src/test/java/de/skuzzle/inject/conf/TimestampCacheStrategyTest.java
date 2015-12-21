package de.skuzzle.inject.conf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TimestampCacheStrategyTest {

    @Mock
    private TextResource resource;

    private final TimestampCacheStrategy subject = new TimestampCacheStrategy();

    @Before
    public void setUp() throws Exception {}

    @Test
    public void testRecacheBinary() throws Exception {
        when(this.resource.getLastModifiedTime()).thenReturn(1L);
        assertTrue(this.subject.refreshBinaryCache(this.resource));

        this.subject.binaryCacheRefreshed(this.resource, new byte[0]);
        assertFalse(this.subject.refreshBinaryCache(this.resource));

        when(this.resource.getLastModifiedTime()).thenReturn(2L);
        assertTrue(this.subject.refreshBinaryCache(this.resource));
    }

    @Test
    public void testRecacheText() throws Exception {
        when(this.resource.getLastModifiedTime()).thenReturn(1L);
        assertTrue(this.subject.refreshTextCache(this.resource));

        this.subject.textCacheRefreshed(this.resource, "");
        assertFalse(this.subject.refreshTextCache(this.resource));

        when(this.resource.getLastModifiedTime()).thenReturn(2L);
        assertTrue(this.subject.refreshTextCache(this.resource));
    }

    @Test
    public void testIndependency1() throws Exception {
        when(this.resource.getLastModifiedTime()).thenReturn(1L);
        this.subject.textCacheRefreshed(this.resource, "");
        assertTrue(this.subject.refreshBinaryCache(this.resource));
        assertFalse(this.subject.refreshTextCache(this.resource));
    }

    @Test
    public void testIndependency2() throws Exception {
        when(this.resource.getLastModifiedTime()).thenReturn(1L);
        this.subject.binaryCacheRefreshed(this.resource, new byte[0]);
        assertFalse(this.subject.refreshBinaryCache(this.resource));
        assertTrue(this.subject.refreshTextCache(this.resource));
    }
}
