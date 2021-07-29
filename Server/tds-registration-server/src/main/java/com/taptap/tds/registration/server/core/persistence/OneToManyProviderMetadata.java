package com.taptap.tds.registration.server.core.persistence;

import java.lang.reflect.Method;

public class OneToManyProviderMetadata {

    private final String mappedStatementId;

    private final Method method;

    public OneToManyProviderMetadata(String mappedStatementId, Method method) {
        this.mappedStatementId = mappedStatementId;
        this.method = method;
    }

    public String getMappedStatementId() {
        return mappedStatementId;
    }

    public Method getMethod() {
        return method;
    }
}
