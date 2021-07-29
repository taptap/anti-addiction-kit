package com.taptap.tds.registration.server.core.manager.interceptor;


import com.taptap.tds.registration.server.core.domain.FieldsExpand;


public abstract class AbstractQueryInterceptor<T> implements QueryInterceptor<T> {

    @Override
    public void postQuery(T entity, FieldsExpand fieldsExpand) {
        if (entity == null) {
            return;
        }
        postQueryInternal(entity, fieldsExpand);
    }

    protected abstract void postQueryInternal(T entity, FieldsExpand fieldsExpand);
}
