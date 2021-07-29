package com.taptap.tds.registration.server.core.persistence.mybatis.paging;

import org.apache.ibatis.session.RowBounds;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class SortableRowBounds extends RowBounds{

    private Sort sort;

    public SortableRowBounds(Pageable pageable) {
        super((int) pageable.getOffset(), pageable.getPageSize());
        this.sort = pageable.getSort();
    }

    public Sort getSort(){
        return sort;
    }


}
