package com.taptap.tds.registration.server.core.persistence.mybatis.manager;

import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.persistence.mybatis.mapper.BaseMapper;
import com.taptap.tds.registration.server.util.Collections3;
import com.taptap.tds.registration.server.util.FieldsExpandUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class CachingManager<T, ID, MAPPER extends BaseMapper<T, ID>, MANAGER extends CachingManager<T, ID, MAPPER, MANAGER>>
        extends BaseManager<T, ID, MAPPER> {

    @Autowired
    private MANAGER manager;

    @Override
    public List<T> findAllByIds(List<ID> ids, FieldsExpand fieldsExpand) {
        if (Collections3.isEmpty(ids)) {
            return Collections.emptyList();
        }
        if (FieldsExpandUtils.hasExpands(fieldsExpand)) {
            return super.findAllByIds(ids, fieldsExpand);
        } else {
            List<T> entities = new ArrayList<>(ids.size());
            for (ID id : ids) {
                Optional<T> optional = manager.get(id);
                if (!optional.isPresent()) {
                    continue;
                }
                entities.add(FieldsExpandUtils.copyEntityWithRequiredProperties(optional.get(), fieldsExpand));
            }
            return entities;
        }
    }
}
