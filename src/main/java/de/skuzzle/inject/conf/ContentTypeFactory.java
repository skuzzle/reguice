package de.skuzzle.inject.conf;

interface ContentTypeFactory {

    TextContentType newJsonContentType();

    TextContentType newPropertiesContentType();

    TextContentType newStringContentType();
}
