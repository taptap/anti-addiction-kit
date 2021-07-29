package com.taptap.tds.registration.server.mapper;


import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.persistence.mybatis.mapper.BaseMapper;
import com.taptap.tds.registration.server.core.persistence.mybatis.provider.BaseSelectProvider;
import com.taptap.tds.registration.server.domain.IdentificationDetails;
import com.taptap.tds.registration.server.enums.IdentificationStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

@Mapper
public interface IdentificationDetailsMapper extends BaseMapper<IdentificationDetails, Long> {

    @ResultMap(BASE_RESULT_MAP_NAME)
    @SelectProvider(type = BaseSelectProvider.class, method = FIND_BY_CONDITION_METHOD)
    IdentificationDetails findByUserId(String userId,  FieldsExpand fieldsExpand);

    @ResultMap(BASE_RESULT_MAP_NAME)
    @SelectProvider(type = BaseSelectProvider.class, method = FIND_BY_CONDITION_METHOD)
    List<IdentificationDetails> findByUserIdIn(List<String> userIds, FieldsExpand fieldsExpand);

    @ResultMap(BASE_RESULT_MAP_NAME)
    @SelectProvider(type = BaseSelectProvider.class, method = FIND_BY_CONDITION_METHOD)
    List<IdentificationDetails> findByStatus(IdentificationStatus status, FieldsExpand fieldsExpand);

//    @ResultMap(BASE_RESULT_MAP_NAME)
//    @SelectProvider(type = BaseSelectProvider.class, method = FIND_BY_CONDITION_METHOD)
//    List<IdentificationDetails> findByNeedCallback(Boolean needCallback, FieldsExpand fieldsExpand);


//    @UpdateProvider(type = BaseUpdateProvider.class, method = UPDATE_BY_CONDITION_METHOD)
//    int updateNeedCallbackByIdIn(Collection<Long> ids, Boolean needCallback);
}
