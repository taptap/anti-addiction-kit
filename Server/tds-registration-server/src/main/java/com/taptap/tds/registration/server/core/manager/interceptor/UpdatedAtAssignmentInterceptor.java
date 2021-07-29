package com.taptap.tds.registration.server.core.manager.interceptor;


import com.taptap.tds.registration.server.core.domain.UpdateTimeAwareEntity;

import java.time.Instant;

public class UpdatedAtAssignmentInterceptor<T> extends AbstractUpdateInterceptor<T> {

    @Override
    protected void preUpdateInternal(T entity) {
        if (!(entity instanceof UpdateTimeAwareEntity)) {
            return;
        }
        ((UpdateTimeAwareEntity) entity).setUpdatedAt(Instant.now());
    }
}
