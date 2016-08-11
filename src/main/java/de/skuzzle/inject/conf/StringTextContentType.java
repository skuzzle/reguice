package de.skuzzle.inject.conf;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.Reader;

import com.google.common.io.CharStreams;
import com.google.inject.ProvisionException;

final class StringTextContentType implements TypedContentType<String> {

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public <T> T createInstance(Class<T> type, TextResource resource) {
        checkArgument(type == String.class, "Text can only be mapped to String.class");

        try (Reader reader = resource.openStream()) {
            final String result = CharStreams.toString(reader);
            // safe cast as by precondition
            return type.cast(result);
        } catch (final IOException e) {
            throw new ProvisionException(String.format(
                    "Error while reading from '%s'", resource), e);
        }
    }
}
