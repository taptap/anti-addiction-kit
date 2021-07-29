package com.taptap.tds.registration.server.core.manager.interceptor;


import com.taptap.tds.registration.server.util.Collections3;

import java.util.Collection;

public abstract class AbstractUpdateInterceptor<T> implements UpdateInterceptor<T> {

    @Override
    public void preUpdate(T entity) {
        if (entity == null) {
            return;
        }
        preUpdateInternal(entity);
    }

    protected abstract void preUpdateInternal(T entity);

    @Override
    public void preUpdate(Collection<T> entities) {
        if (Collections3.isEmpty(entities)) {
            return;
        }
        for (T entity : entities) {
            preUpdate(entity);
        }
    }
}
