package de.skuzzle.inject.conf;

import java.io.File;

/**
 * When binding to interfaces, a converter can parse a String to a more complex
 * type like for example a {@link File}.
 *
 * @author Simon Taddiken
 * @param <T> The target type.
 */
public interface Converter<T> {

    /**
     * The type that this converter can create from a String.
     *
     * @return The type.
     */
    Class<T> forType();

    /**
     * Handles the creation of an object of type given by {@link #forType()}
     * from the provided String.
     *
     * @param s The String to parse.
     * @return The created object.
     * @throws ConverterException When the given String can not be coerced to an
     *             instance of the type returned by {@link #forType()}.
     */
    T parseString(String s) throws ConverterException;
}
