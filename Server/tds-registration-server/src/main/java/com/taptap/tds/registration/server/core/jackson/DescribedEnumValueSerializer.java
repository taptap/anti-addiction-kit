package com.taptap.tds.registration.server.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.taptap.tds.registration.server.core.enums.DescribedEnumValue;

import java.io.IOException;

public class DescribedEnumValueSerializer extends JsonSerializer<DescribedEnumValue<?>> {

    @Override
    public void serialize(DescribedEnumValue<?> describedEnumValue, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {

        gen.writeStartObject();
        gen.writeObjectField("value", describedEnumValue.getValue().toString());
        gen.writeStringField("label", describedEnumValue.getDescription());
        gen.writeEndObject();
    }
}
