package de.skuzzle.inject.conf;

import java.io.IOException;

final class TimestampCacheStrategy implements CachingStrategy {

    private long lastCharsRead;
    private long lastBytesRead;

    @Override
    public void textCacheRefreshed(TextResource resource, String bufferedString)
            throws IOException {
        this.lastCharsRead = resource.getLastModifiedTime();
    }

    @Override
    public void binaryCacheRefreshed(TextResource resource, byte[] bufferedbytes)
            throws IOException {
        this.lastBytesRead = resource.getLastModifiedTime();
    }

    @Override
    public boolean refreshTextCache(TextResource resource) throws IOException {
        return checkTimestamp(resource, this.lastCharsRead);
    }

    @Override
    public boolean refreshBinaryCache(TextResource resource) throws IOException {
        return checkTimestamp(resource, this.lastBytesRead);
    }

    private static boolean checkTimestamp(TextResource resource, long compare)
            throws IOException {
        return resource.getLastModifiedTime() > compare;
    }
}
