package com.taptap.tds.registration.server.core.persistence.mybatis.mapper;

import com.taptap.tds.registration.server.core.persistence.mybatis.provider.BaseDeleteProvider;
import org.apache.ibatis.annotations.DeleteProvider;

import java.util.List;

public interface BaseDeleteMapper<T, ID> {

    public static final String DELETE_METHOD = "delete";

    public static final String BULK_DELETE_METHOD = "bulkDelete";

    public static final String DELETE_BY_CONDITION_METHOD = "deleteByCondition";

    @DeleteProvider(type = BaseDeleteProvider.class, method = DELETE_METHOD)
    int deleteById(ID id);

    @DeleteProvider(type = BaseDeleteProvider.class, method = DELETE_METHOD)
    int delete(T entity);

    @DeleteProvider(type = BaseDeleteProvider.class, method = BULK_DELETE_METHOD)
    int deleteByIds(List<ID> ids);

    @DeleteProvider(type = BaseDeleteProvider.class, method = BULK_DELETE_METHOD)
    int bulkDelete(List<T> entities);
}
