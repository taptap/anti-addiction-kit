package com.taptap.tds.registration.server.core.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.taptap.tds.registration.server.core.enums.EnumValue;
import com.taptap.tds.registration.server.core.enums.EnumValueFactory;
import com.taptap.tds.registration.server.core.enums.EnumValueUtils;

import java.io.IOException;

public class EnumValueDeserializer extends JsonDeserializer<EnumValue<?>> implements ContextualDeserializer {

    private Class<? extends EnumValue<?>> enumValueClass;

    private Class<?> valueType;

    public void setEnumValueClass(Class<? extends EnumValue<?>> enumValueClass) {
        this.enumValueClass = enumValueClass;
    }

    public void setValueType(Class<?> valueType) {
        this.valueType = valueType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        @SuppressWarnings("unchecked")
        Class<? extends EnumValue<?>> enumValueClass = (Class<? extends EnumValue<?>>) ctxt.getContextualType().getRawClass();
        EnumValueDeserializer enumValueDeserializer = new EnumValueDeserializer();
        enumValueDeserializer.setEnumValueClass(enumValueClass);
        enumValueDeserializer.setValueType(EnumValueUtils.getEnumValueActualType(enumValueClass));
        return enumValueDeserializer;
    }

    @Override
    public EnumValue<?> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {

        Object value = null;
        if (parser.isExpectedStartObjectToken()) {
            JsonNode node = parser.readValueAsTree();
            value = getValueNodeValue(node.get("value"));
        } else {
            value = parser.readValueAs(valueType);
        }
        return deserializeInternal(value);
    }

    protected Object getValueNodeValue(JsonNode node) {
        return node.asText();
    }

    protected EnumValue<?> deserializeInternal(Object value) {
        return EnumValueFactory.get(enumValueClass, value).orElse(null);
    }
}
