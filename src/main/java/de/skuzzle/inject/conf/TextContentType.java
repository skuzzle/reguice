package de.skuzzle.inject.conf;

import de.skuzzle.inject.conf.Resources.ChooseTargetType;

/**
 * This interfaces controls how the content of a {@link TextResource} is mapped
 * to a concrete instance of some object.
 *
 * @author Simon Taddiken
 */
public interface TextContentType {

    /**
     * Creates an instance of type T from the given resource. The type that is
     * passed here, is the one that the user passed to
     * {@link ChooseTargetType#to(Class)} when binding a resource.
     *
     * @param <T> The result type.
     * @param type The type to create an instance of.
     * @param resource The resource to create an instance from.
     * @return The created Object.
     */
    <T> T createInstance(Class<T> type, TextResource resource);
}
