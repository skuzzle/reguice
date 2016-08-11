package de.skuzzle.inject.conf;

import com.google.inject.Binder;

/**
 * A content type that produces only objects that are assignable to a certain type.
 *
 * @author Simon Taddiken
 */
public interface TypedContentType<E> extends TextContentType {

    /**
     * The type of objects produced by {@link #createInstance(Class, TextResource)}.
     *
     * @return The type.
     */
    Class<E> getType();

    /**
     * {@inheritDoc}
     * <p>
     * This method must only return objects of the type returned by {@link #getType()}.
     * </p>
     *
     * @param <T> The result type.
     * @param type The type to create an instance of. This is the Class part of the key
     *            which has been bound at the {@link Binder}.
     * @param resource The resource to create an instance from.
     * @return The created Object.
     */
    @Override
    <T> T createInstance(Class<T> type, TextResource resource);
}
