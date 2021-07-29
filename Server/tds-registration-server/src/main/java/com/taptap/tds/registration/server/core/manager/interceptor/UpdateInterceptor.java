package com.taptap.tds.registration.server.core.manager.interceptor;


import com.taptap.tds.registration.server.util.Collections3;

import java.util.Collection;

public interface UpdateInterceptor<T> {

    void preUpdate(T entity);

    default void preUpdate(Collection<T> entities) {
        if (Collections3.isEmpty(entities)) {
            return;
        }
        for (T entity : entities) {
            preUpdate(entity);
        }
    }
}
