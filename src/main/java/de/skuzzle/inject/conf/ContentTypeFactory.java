package de.skuzzle.inject.conf;

import com.google.gson.GsonBuilder;

/**
 * Internal factory class for creating different kinds of
 * {@link TextContentType}.
 *
 * @author Simon Taddiken
 */
interface ContentTypeFactory {

    TextContentType newJsonContentType(GsonBuilder builder);

    TextContentType newPropertiesContentType();

    TextContentType newStringContentType();
}
