package com.taptap.tds.registration.server.core.datastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

/**
 * @Author guyu
 * @create 2020/12/10 8:44 下午
 */
public class InMemoryDataStore<T> implements DataStore<T> {

    private static final int DEFAULT_MAX_ELEMENTS = 500;

    private final BlockingQueue<T> queue;

    private final Function<Integer, Collection<T>> collectionSupplier;

    private final int maxElements;

    public InMemoryDataStore() {
        this(ArrayList::new);
    }

    public InMemoryDataStore(int maxElements) {
        this(ArrayList::new, maxElements);
    }

    public InMemoryDataStore(Function<Integer, Collection<T>> collectionSupplier) {
        this(new LinkedBlockingQueue<>(), collectionSupplier);
    }

    public InMemoryDataStore(Function<Integer, Collection<T>> collectionSupplier, int maxElements) {
        this(new LinkedBlockingQueue<>(), collectionSupplier, maxElements);
    }

    public InMemoryDataStore(BlockingQueue<T> queue, Function<Integer, Collection<T>> collectionSupplier) {
        this(queue, collectionSupplier, DEFAULT_MAX_ELEMENTS);
    }

    public InMemoryDataStore(BlockingQueue<T> queue, Function<Integer, Collection<T>> collectionSupplier, int maxElements) {
        Objects.requireNonNull(queue);
        Objects.requireNonNull(collectionSupplier);
        this.queue = queue;
        this.collectionSupplier = collectionSupplier;
        this.maxElements = maxElements;
    }

    @Override
    public void store(Collection<T> datas) {
        queue.addAll(datas);
    }

    @Override
    public Collection<T> bulkRetrieve() {
        if (queue.isEmpty()) {
            return Collections.emptyList();
        }
        int maxElements = Math.min(queue.size(), this.maxElements);
        Collection<T> datas = collectionSupplier.apply(maxElements);
        queue.drainTo(datas, maxElements);
        return datas;
    }
}
