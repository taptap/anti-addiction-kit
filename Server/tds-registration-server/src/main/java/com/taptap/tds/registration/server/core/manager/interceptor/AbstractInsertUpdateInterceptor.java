package com.taptap.tds.registration.server.core.manager.interceptor;


public abstract class AbstractInsertUpdateInterceptor<T> implements InsertUpdateInterceptor<T> {

    @Override
    public void preInsert(T entity) {
        if (entity == null) {
            return;
        }
        preHandle(entity);
    }

    protected abstract void preHandle(T entity);

    @Override
    public void preUpdate(T entity) {
        if (entity == null) {
            return;
        }
        preHandle(entity);
    }
}
