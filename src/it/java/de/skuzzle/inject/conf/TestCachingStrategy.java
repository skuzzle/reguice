package de.skuzzle.inject.conf;

import java.io.IOException;

public class TestCachingStrategy implements CachingStrategy {

    @Override
    public void textCacheRefreshed(TextResource resource, String bufferedString)
            throws IOException {}

    @Override
    public void binaryCacheRefreshed(TextResource resource, byte[] bufferedBytes)
            throws IOException {}

    @Override
    public boolean refreshTextCache(TextResource resource) throws IOException {
        return false;
    }

    @Override
    public boolean refreshBinaryCache(TextResource resource) throws IOException {
        return false;
    }

}
