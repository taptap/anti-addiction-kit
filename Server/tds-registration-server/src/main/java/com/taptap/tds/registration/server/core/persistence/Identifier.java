package com.taptap.tds.registration.server.core.persistence;

import java.lang.reflect.Method;

public class Identifier {

    private final String name;

    private final Class<?> type;

    private final Method getterMethod;

    private final Method setterMethod;

    public Identifier(String name, Class<?> type, Method getterMethod, Method setterMethod) {
        this.name = name;
        this.type = type;
        this.getterMethod = getterMethod;
        this.setterMethod = setterMethod;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public Method getGetterMethod() {
        return getterMethod;
    }

    public Method getSetterMethod() {
        return setterMethod;
    }
}
