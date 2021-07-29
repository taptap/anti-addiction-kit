package com.taptap.tds.registration.server.core.enums;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.taptap.tds.registration.server.core.jackson.EnumValueDeserializer;

@JsonDeserialize(using = EnumValueDeserializer.class)
public interface EnumValue<T> {

    T getValue();
}
