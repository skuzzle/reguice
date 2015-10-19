package de.skuzzle.inject.conf;

public interface TextContentType {

    <T> T createInstance(Class<T> type, TextResource resource);
}
