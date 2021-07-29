package com.taptap.tds.registration.server.core.manager.interceptor;


import com.taptap.tds.registration.server.util.Collections3;

import java.util.Collection;

public interface InsertInterceptor<T> {

    void preInsert(T entity);

    default void preInsert(Collection<T> entities) {
        if (Collections3.isEmpty(entities)) {
            return;
        }
        for (T entity : entities) {
            preInsert(entity);
        }
    }
}
