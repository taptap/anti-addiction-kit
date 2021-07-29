package com.taptap.tds.registration.server.core.persistence.mybatis.mapper;


public interface BaseMapper<T, ID>
        extends BaseSelectMapper<T, ID>, PagingMapper<T>, BaseInsertMapper<T>, BaseUpdateMapper<T>, BaseDeleteMapper<T, ID> {
}
