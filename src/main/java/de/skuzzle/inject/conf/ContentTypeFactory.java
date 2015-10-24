package de.skuzzle.inject.conf;

/**
 * Internal factory class for creating different kinds of
 * {@link TextContentType}.
 *
 * @author Simon Taddiken
 */
interface ContentTypeFactory {

    TextContentType newJsonContentType();

    TextContentType newPropertiesContentType();

    TextContentType newStringContentType();
}
