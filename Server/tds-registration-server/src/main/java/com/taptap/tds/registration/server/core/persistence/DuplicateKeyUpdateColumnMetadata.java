package com.taptap.tds.registration.server.core.persistence;

public class DuplicateKeyUpdateColumnMetadata {

    private final String column;

    private final boolean nullable;

    public DuplicateKeyUpdateColumnMetadata(String column, boolean nullable) {
        this.column = column;
        this.nullable = nullable;
    }

    public String getColumn() {
        return column;
    }

    public boolean isNullable() {
        return nullable;
    }
}
