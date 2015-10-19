package de.skuzzle.inject.conf;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import com.google.inject.ProvisionException;

final class StringTextContentType implements TextContentType {
    private static final int BUFFER_SIZE = 4 * 1024;

    @Override
    public <T> T createInstance(Class<T> type, TextResource resource) {
        checkArgument(type == String.class, "Text can only be mapped to String.class");
        final StringWriter w = new StringWriter();
        try (Reader reader = resource.openStream()) {
            final char[] buffer = new char[BUFFER_SIZE];

            int len = 0;
            while ((len = reader.read(buffer)) != -1) {
                w.write(buffer, 0, len);
            }

            // safe cast as by precondition
            return type.cast(w.toString());
        } catch (final IOException e) {
            throw new ProvisionException(String.format(
                    "Error while reading from '%s'", resource), e);
        }
    }
}
