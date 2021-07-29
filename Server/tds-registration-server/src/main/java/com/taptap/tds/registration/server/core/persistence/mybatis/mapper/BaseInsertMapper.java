package com.taptap.tds.registration.server.core.persistence.mybatis.mapper;

import com.taptap.tds.registration.server.core.persistence.mybatis.provider.BaseInsertProvider;
import org.apache.ibatis.annotations.InsertProvider;

import java.util.List;

public interface BaseInsertMapper<T> {

    public static final String INSERT_METHOD = "insert";

    public static final String BULK_INSERT_METHOD = "bulkInsert";

    @InsertProvider(type = BaseInsertProvider.class, method = INSERT_METHOD)
    int insert(T entity);

    @InsertProvider(type = BaseInsertProvider.class, method = BULK_INSERT_METHOD)
    int bulkInsert(List<T> entities);
}
