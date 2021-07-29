package com.taptap.tds.registration.server.enums;


import com.taptap.tds.registration.server.core.enums.EnumValue;

public enum UserType implements EnumValue<Byte> {
    AUTHENTICATED_USER(0),
    TOURIST(2);

    private final Byte value;

    private UserType(int i) {
        this.value = (byte)i;
    }

    public Byte getValue() {
        return this.value;
    }
}
