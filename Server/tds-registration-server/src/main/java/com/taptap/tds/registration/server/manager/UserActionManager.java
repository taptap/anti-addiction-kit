package com.taptap.tds.registration.server.manager;

import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.persistence.mybatis.manager.BaseManager;
import com.taptap.tds.registration.server.domain.UserAction;
import com.taptap.tds.registration.server.mapper.UserActionMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class UserActionManager extends BaseManager<UserAction, Long, UserActionMapper> {

    public List<UserAction> findByPushSuccessAndActionTimeGreaterThanEqual(boolean pushSuccess, Instant actionTime, FieldsExpand fieldsExpand) {
        return mapper.findByPushSuccessAndActionTimeGreaterThanEqual(pushSuccess, actionTime, fieldsExpand);
    }

    public int updatePushSuccessByIdIn(List<Long> ids, boolean pushSuccess) {
        return mapper.updatePushSuccessByIdIn(ids, pushSuccess);
    }
}