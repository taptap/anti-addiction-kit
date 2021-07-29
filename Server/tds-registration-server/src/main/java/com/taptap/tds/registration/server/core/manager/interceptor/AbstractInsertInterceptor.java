package com.taptap.tds.registration.server.core.manager.interceptor;



public abstract class AbstractInsertInterceptor<T> implements InsertInterceptor<T> {

    @Override
    public void preInsert(T entity) {
        if (entity == null) {
            return;
        }
        preInsertInternal(entity);
    }

    protected abstract void preInsertInternal(T entity);
}
