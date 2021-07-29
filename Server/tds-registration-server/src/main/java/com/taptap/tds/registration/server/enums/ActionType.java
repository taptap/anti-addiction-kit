package com.taptap.tds.registration.server.enums;


import com.taptap.tds.registration.server.core.enums.EnumValue;

public enum ActionType implements EnumValue<Byte> {
    LOGIN(1),
    LOGOUT(0);

    private final Byte value;

    private ActionType(int i) {
        this.value = (byte)i;
    }

    public Byte getValue() {
        return this.value;
    }
}
