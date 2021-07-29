package com.taptap.tds.registration.server.core.manager.interceptor;


import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.util.Collections3;

import java.util.Collection;

public interface QueryInterceptor<T> {

    void postQuery(T entity, FieldsExpand fieldsExpand);

    default void postQuery(Collection<T> entities, FieldsExpand fieldsExpand) {
        if (Collections3.isEmpty(entities)) {
            return;
        }

        for (T entity : entities) {
            postQuery(entity, fieldsExpand);
        }
    }
}
