package com.taptap.tds.registration.server.core.enums;

public final class EnumInfo {

    private final String label;

    private final Object value;

    public EnumInfo(DescribedEnumValue<?> enumValue) {
        this.label = enumValue.getDescription();
        this.value = enumValue.getValue();
    }

    public EnumInfo(String label, Object value, String color) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public Object getValue() {
        return value;
    }

}