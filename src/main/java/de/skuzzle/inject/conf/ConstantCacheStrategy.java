package de.skuzzle.inject.conf;

final class ConstantCacheStrategy implements CachingStrategy {

    private static final CachingStrategy INSTANCE = new ConstantCacheStrategy();

    private ConstantCacheStrategy() {}

    static CachingStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public void textCacheRefreshed(TextResource resource, String bufferedString) {}

    @Override
    public void binaryCacheRefreshed(TextResource resource, byte[] bufferedBytes) {}

    @Override
    public boolean refreshTextCache(TextResource resource) {
        return false;
    }

    @Override
    public boolean refreshBinaryCache(TextResource resource) {
        return false;
    }

}
