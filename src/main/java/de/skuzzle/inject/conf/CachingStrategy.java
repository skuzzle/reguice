package de.skuzzle.inject.conf;

import java.io.IOException;

/**
 * Controls when a cached resource's data will be refreshed. Implementations
 * must either be state less or depend their state on the {@link TextResource}
 * instance that is passed into every method. If an implementation is not state less it
 * must be thread safe for general purpose use.
 * <p>
 * A cached resource separately caches its binary and its text content.
 * </p>
 *
 * @author Simon Taddiken
 * @since 0.3.0
 */
public interface CachingStrategy {

    /**
     * Called right after the text cache of the given resource has been
     * refreshed.
     *
     * @param resource The resource for which the binary cache has been updated.
     * @param bufferedString The string that has just been cached.
     * @throws IOException Can be thrown by implementors if required.
     */
    void textCacheRefreshed(TextResource resource, String bufferedString)
            throws IOException;

    /**
     * Called right after the binary cache of the given resource has been
     * refreshed. The bytes passed to this method are the raw, unmodified bytes
     * that are now stored in the cache. Modifying this data will directly
     * affect the cache and thus all subsequent calls to
     * {@link TextResource#openBinaryStream()}.
     *
     * @param resource The resource for which the binary cache has been updated.
     * @param bufferedBytes The bytes that are now stored.
     * @throws IOException Can be thrown by implementors if required.
     */
    void binaryCacheRefreshed(TextResource resource, byte[] bufferedBytes)
            throws IOException;

    /**
     * Determines whether the text content cache of the given resource shall be
     * refreshed.
     *
     * @param resource The resource to check for.
     * @return Whether the text cache shall be refreshed.
     * @throws IOException If the check causes an IO error.
     */
    boolean refreshTextCache(TextResource resource) throws IOException;

    /**
     * Determines whether the binary content cache of the given resource shall
     * be refreshed.
     *
     * @param resource The resource to check for.
     * @return Whether the binary cache shall be refreshed.
     * @throws IOException If the check causes an IO error.
     */
    boolean refreshBinaryCache(TextResource resource) throws IOException;
}
