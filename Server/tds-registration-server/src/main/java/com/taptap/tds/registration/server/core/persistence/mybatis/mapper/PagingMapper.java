package com.taptap.tds.registration.server.core.persistence.mybatis.mapper;


import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

public interface PagingMapper<T> {

    long countByPage(FieldsExpand query);

    List<T> findByPage(FieldsExpand query, RowBounds rowBounds);
}
