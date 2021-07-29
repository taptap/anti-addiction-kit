package com.taptap.tds.registration.server.core.datastore;

import java.util.Collection;
import java.util.Collections;

/**
 * @Author guyu
 * @create 2020/12/10 8:43 下午
 */
public interface DataStore<T> {

    default void store(T data) {
        store(Collections.singleton(data));
    }

    void store(Collection<T> datas);

    Collection<T> bulkRetrieve();

}
