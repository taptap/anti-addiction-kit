package com.taptap.tds.registration.server.core.persistence.mybatis.mapper;

import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.persistence.mybatis.provider.BaseSelectProvider;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

public interface BaseSelectMapper<T, ID> {

    public static final String BASE_RESULT_MAP_NAME = "BaseResultMap";

    public static final String FIND_ONE_METHOD = "findOne";

    public static final String FIND_BY_CONDITION_METHOD = "findByCondition";

    public static final String EXISTS_METHOD = "exists";

    public static final String EXISTS_BY_CONDITION_METHOD = "existsByCondition";

    public static final String FIND_ALL_METHOD = "findAll";

    public static final String FIND_ALL_BY_IDS_METHOD = "findAllByIds";

    public static final String COUNT_METHOD = "count";

    public static final String COUNT_BY_CONDITION_METHOD = "countByCondition";

    @ResultMap(BASE_RESULT_MAP_NAME)
    @SelectProvider(type = BaseSelectProvider.class, method = FIND_ONE_METHOD)
    T findOne(ID id, FieldsExpand fieldsExpand);

    @SelectProvider(type = BaseSelectProvider.class, method = EXISTS_METHOD)
    boolean exists(ID id);

    @ResultMap(BASE_RESULT_MAP_NAME)
    @SelectProvider(type = BaseSelectProvider.class, method = FIND_ALL_METHOD)
    List<T> findAll(FieldsExpand fieldsExpand);

    @ResultMap(BASE_RESULT_MAP_NAME)
    @SelectProvider(type = BaseSelectProvider.class, method = FIND_ALL_BY_IDS_METHOD)
    List<T> findAllByIds(List<ID> ids, FieldsExpand fieldsExpand);

    @SelectProvider(type = BaseSelectProvider.class, method = COUNT_METHOD)
    long count();
}
