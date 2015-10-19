package de.skuzzle.inject.conf;

import java.io.IOException;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.inject.ProvisionException;

class JsonContentType implements TextContentType {

    @Override
    public <T> T createInstance(Class<T> type, TextResource resource) {
        final Gson gson = new Gson();
        try (Reader reader  = resource.openStream()) {
            return gson.fromJson(reader, type);
        } catch (final IOException e) {
            throw new ProvisionException("Error while deserializing from json", e);
        }
    }

}
