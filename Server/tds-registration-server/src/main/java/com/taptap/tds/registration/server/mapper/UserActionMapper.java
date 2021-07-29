package com.taptap.tds.registration.server.mapper;

import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.persistence.mybatis.mapper.BaseMapper;
import com.taptap.tds.registration.server.core.persistence.mybatis.provider.BaseSelectProvider;
import com.taptap.tds.registration.server.core.persistence.mybatis.provider.BaseUpdateProvider;
import com.taptap.tds.registration.server.domain.UserAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Mapper
public interface UserActionMapper extends BaseMapper<UserAction, Long> {

    @ResultMap(BASE_RESULT_MAP_NAME)
    @SelectProvider(type = BaseSelectProvider.class, method = FIND_BY_CONDITION_METHOD)
    List<UserAction> findByPushSuccessAndActionTimeGreaterThanEqual(boolean pushSuccess, Instant actionTime, FieldsExpand fieldsExpand);

    @UpdateProvider(type = BaseUpdateProvider.class, method = UPDATE_BY_CONDITION_METHOD)
    int updatePushSuccessByIdIn(Collection<Long> ids, boolean pushSuccess);
}