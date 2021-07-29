package com.taptap.tds.registration.server.core.persistence.mybatis.mapper;

import com.taptap.tds.registration.server.core.persistence.mybatis.provider.BaseUpdateProvider;
import org.apache.ibatis.annotations.UpdateProvider;

public interface BaseUpdateMapper<T> {

    public static final String UPDATE_METHOD = "update";

    public static final String UPDATE_NON_NULL_METHOD = "updateNonNull";

    public static final String UPDATE_BY_CONDITION_METHOD = "updateByCondition";

    @UpdateProvider(type = BaseUpdateProvider.class, method = UPDATE_METHOD)
    int update(T entity);

    @UpdateProvider(type = BaseUpdateProvider.class, method = UPDATE_NON_NULL_METHOD)
    int updateNonNull(T entity);
}
