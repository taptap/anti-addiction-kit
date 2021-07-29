package com.taptap.tds.registration.server.util;

import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public final class Collections3 {

    private Collections3() {
        throw new AssertionError();
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static int size(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

    public static <T> List<T> asList(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        if (collection instanceof List) {
            return (List<T>) collection;
        } else {
            return new ArrayList<>(collection);
        }
    }

    public static <T, R> List<R> transformToList(T[] array, Function<T, R> mapper) {
        return transformToCollection(array, mapper, ArrayList::new);
    }

    public static <T, R> List<R> transformToList(Collection<T> collection, Function<T, R> mapper) {
        return transformToCollection(collection, mapper, ArrayList::new);
    }

    public static <T, R> Set<R> transformToSet(T[] array, Function<T, R> mapper) {
        return transformToCollection(array, mapper, HashSet::new);
    }

    public static <T, R> Set<R> transformToSet(Collection<T> collection, Function<T, R> mapper) {
        return transformToCollection(collection, mapper, HashSet::new);
    }

    public static <T, R, C extends Collection<R>> C transformToCollection(Collection<T> collection, Function<T, R> mapper,
            Supplier<C> collectionFactory) {
        return isEmpty(collection) ? collectionFactory.get()
                : collection.stream().map(mapper).filter(Objects::nonNull).collect(Collectors.toCollection(collectionFactory));
    }

    public static <T, R, C extends Collection<R>> C transformToCollection(T[] array, Function<T, R> mapper,
            Supplier<C> collectionFactory) {
        return ArrayUtils.isEmpty(array) ? collectionFactory.get()
                : Stream.of(array).collect(Collectors.mapping(mapper, Collectors.toCollection(collectionFactory)));
    }

    public static <K, V> Map<K, V> transformToMap(Collection<V> collection, Function<V, K> keyMapper) {
        return transformToMap(collection, keyMapper, Function.identity());
    }

    public static <T, K, V> Map<K, V> transformToMap(Collection<T> collection, Function<T, K> keyMapper,
            Function<T, V> valueMapper) {
        return isEmpty(collection) ? Collections.emptyMap()
                : collection.stream().collect(Collectors.toMap(keyMapper, valueMapper));
    }

    public static <K, V> Map<K, List<V>> groupingBy(Collection<V> collection, Function<V, K> classifier) {
        return isEmpty(collection) ? Collections.emptyMap() : collection.stream().collect(Collectors.groupingBy(classifier));
    }

    public static <K, V, M extends Map<K, List<V>>> Map<K, List<V>> groupingBy(Collection<V> collection,
            Function<V, K> classifier, Supplier<M> mapFactory) {
        return isEmpty(collection) ? Collections.emptyMap()
                : collection.stream().collect(Collectors.groupingBy(classifier, mapFactory, Collectors.toList()));
    }

    public static <K, V, T> Map<K, List<V>> groupingBy(Collection<? extends T> collection, Function<T, K> keyTransformer,
            Function<T, V> valueTransformer) {
        return groupingBy(collection, keyTransformer, valueTransformer, HashMap::new);
    }

    public static <K, V, T, M extends Map<K, List<V>>> Map<K, List<V>> groupingBy(Collection<? extends T> collection,
            Function<T, K> keyTransformer, Function<T, V> valueTransformer, Supplier<M> mapFactory) {

        return isEmpty(collection) ? Collections.emptyMap()
                : collection.stream().collect(Collectors.groupingBy(keyTransformer, mapFactory,
                        Collectors.mapping(valueTransformer, Collectors.toList())));
    }

    public static <T> BigDecimal summarizingBigDeciaml(Collection<T> collection, Function<? super T, BigDecimal> mapper) {
        return collection == null ? null : collection.stream().map(mapper).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
