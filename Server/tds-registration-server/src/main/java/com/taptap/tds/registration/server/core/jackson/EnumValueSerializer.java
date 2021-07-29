package com.taptap.tds.registration.server.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.taptap.tds.registration.server.core.enums.EnumValue;

import java.io.IOException;


public class EnumValueSerializer extends JsonSerializer<EnumValue<?>> {

    @Override
    public void serialize(EnumValue<?> enumValue, JsonGenerator generator, SerializerProvider provider) throws IOException {

        Object value = null;
        if (enumValue != null) {
            value = enumValue.getValue();
        }
        generator.writeObject(value);
    }
}
