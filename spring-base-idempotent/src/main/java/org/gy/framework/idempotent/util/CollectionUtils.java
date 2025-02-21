package org.gy.framework.idempotent.util;

import cn.hutool.core.collection.CollUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CollectionUtils {

    private CollectionUtils(){}

    public static <T, K, V> Map<K, V> convertMap(Collection<T> from, Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return convertMap(from, keyFunc, valueFunc, (v1, v2) -> v1);
    }

    public static <T, K, V> Map<K, V> convertMap(Collection<T> from, Function<T, K> keyFunc, Function<T, V> valueFunc, BinaryOperator<V> mergeFunction) {
        return convertMap(from, keyFunc, valueFunc, mergeFunction, HashMap::new);
    }

    public static <T, K, V> Map<K, V> convertMap(Collection<T> from, Function<T, K> keyFunc, Function<T, V> valueFunc, BinaryOperator<V> mergeFunction, Supplier<? extends Map<K, V>> supplier) {
        if (CollUtil.isEmpty(from)) {
            return Collections.emptyMap();
        }
        return from.stream().collect(Collectors.toMap(keyFunc, valueFunc, mergeFunction, supplier));
    }
}
