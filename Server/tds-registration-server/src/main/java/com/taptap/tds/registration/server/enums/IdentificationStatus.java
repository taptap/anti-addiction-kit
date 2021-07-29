package com.taptap.tds.registration.server.enums;


import com.taptap.tds.registration.server.core.enums.EnumValue;

public enum IdentificationStatus implements EnumValue<Byte> {

    SUCCESS(0), IDENTIFYING(1),
    FAILED(2);

    private final Byte value;

    IdentificationStatus(int i) {
        this.value = (byte)i;
    }

    public Byte getValue() {
        return this.value;
    }

}
