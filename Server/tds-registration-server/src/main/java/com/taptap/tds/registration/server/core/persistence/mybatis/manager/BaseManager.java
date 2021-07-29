package com.taptap.tds.registration.server.core.persistence.mybatis.manager;

import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.manager.AbstractManager;
import com.taptap.tds.registration.server.core.persistence.mybatis.mapper.BaseMapper;
import com.taptap.tds.registration.server.core.persistence.mybatis.paging.PagingUtils;
import com.taptap.tds.registration.server.util.Collections3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class BaseManager<T, ID, MAPPER extends BaseMapper<T, ID>> extends AbstractManager<T> {

    @Autowired
    protected MAPPER mapper;

    public BaseManager() {
    }

    public Optional<T> get(ID id) {
        return get(id, null);
    }

    public Optional<T> get(ID id, FieldsExpand fieldsExpand) {
        T entity = mapper.findOne(id, fieldsExpand);
        postQuery(entity, fieldsExpand);
        return Optional.ofNullable(entity);
    }

    public boolean exists(ID id) {
        return mapper.exists(id);
    }

    public List<T> findAll() {
        return findAll(null);
    }

    public List<T> findAll(FieldsExpand fieldsExpand) {
        List<T> entities = mapper.findAll(fieldsExpand);
        postQuery(entities, fieldsExpand);
        return entities;
    }

    public List<T> findAllByIds(List<ID> ids) {
        return findAllByIds(ids, null);
    }

    public List<T> findAllByIds(List<ID> ids, FieldsExpand fieldsExpand) {
        List<T> entities = mapper.findAllByIds(ids, fieldsExpand);
        postQuery(entities, fieldsExpand);
        return entities;
    }

    public long count() {
        return mapper.count();
    }

    public Page<T> findByPage(FieldsExpand query, Pageable pageable) {
        Page<T> page = PagingUtils.findByPage(mapper, query, pageable);
        postQuery(page.getContent(), query);
        return page;
    }

    public int save(T entity) {
        if (entity == null) {
            return 0;
        }
        preInsert(entity);
        return mapper.insert(entity);
    }

    @Transactional
    public int bulkSave(Collection<T> entities) {
        if (Collections3.isEmpty(entities)) {
            return 0;
        }
        preInsert(entities);

        List<T> entityList;
        if (entities instanceof List) {
            entityList = (List<T>) entities;
        } else {
            entityList = new ArrayList<>(entities);
        }

        if (entities.size() <= 500) {
            return mapper.bulkInsert(entityList);
        }

        int size = entityList.size();
        int result = 0;
        for (int i = 0; i < size; i += 500) {
            result += mapper.bulkInsert(entityList.subList(i, Math.min(i + 500, size)));
        }
        return result;
    }

    public int update(T entity) {
        if (entity == null) {
            return 0;
        }
        preUpdate(entity);
        return mapper.update(entity);
    }

    public int updateNonNull(T entity) {
        if (entity == null) {
            return 0;
        }
        preUpdate(entity);
        return mapper.updateNonNull(entity);
    }

    public int deleteById(ID id) {
        return mapper.deleteById(id);
    }

    public int delete(T entity) {
        return mapper.delete(entity);
    }

    public int bulkDeleteByIds(List<ID> ids) {
        return Collections3.isEmpty(ids) ? 0 : mapper.deleteByIds(ids);
    }

    public int bulkDelete(List<T> entities) {
        return Collections3.isEmpty(entities) ? 0 : mapper.bulkDelete(entities);
    }
}
