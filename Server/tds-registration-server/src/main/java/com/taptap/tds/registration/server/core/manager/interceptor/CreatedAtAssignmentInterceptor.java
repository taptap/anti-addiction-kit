package com.taptap.tds.registration.server.core.manager.interceptor;


import com.taptap.tds.registration.server.core.domain.CreateTimeAwareEntity;

import java.time.Instant;


public class CreatedAtAssignmentInterceptor<T> extends AbstractInsertInterceptor<T> {

    @Override
    protected void preInsertInternal(T entity) {
        if (!(entity instanceof CreateTimeAwareEntity)) {
            return;
        }
        ((CreateTimeAwareEntity) entity).setCreatedAt(Instant.now());
    }
}
