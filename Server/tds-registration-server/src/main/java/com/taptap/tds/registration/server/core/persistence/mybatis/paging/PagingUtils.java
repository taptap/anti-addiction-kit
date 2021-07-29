package com.taptap.tds.registration.server.core.persistence.mybatis.paging;

import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.persistence.mybatis.mapper.PagingMapper;
import org.apache.ibatis.session.RowBounds;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PagingUtils {

    public static <T> Page<T> findByPage(final PagingMapper<T> mapper, FieldsExpand query, Pageable pageable) {
        return findByPage(query, pageable, mapper::countByPage, mapper::findByPage);
    }

    public static <T> Page<T> findByPage(FieldsExpand query, Pageable pageable, Function<FieldsExpand, Long> countByPage,
            BiFunction<FieldsExpand, RowBounds, List<T>> findByPage) {

        long totalCount = countByPage.apply(query);
        List<T> content = Collections.emptyList();
        if (totalCount != 0) {
            content = findByPage.apply(query, new SortableRowBounds(pageable));
        }
        return new PageImpl<>(content, pageable, totalCount);
    }
}
