package org.gy.framework.core.support;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 枚举标准定义，统一格式
 *
 * @author gy
 * @version 1.0.0
 */
public interface IStdEnum<T> {

    /**
     * 返回枚举实际值
     */
    T getCode();

    /**
     * 返回枚举描述说明
     */
    String getDesc();

    /**
     * 对比当前枚举对象code与传入值是否相等
     *
     * @param enumCode 枚举code
     * @return code是否相等
     */
    default boolean codeEquals(T enumCode) {
        if (null == enumCode) {
            return false;
        }
        return Objects.equals(getCode(), enumCode);
    }

    /**
     * 根据code获取枚举定义
     *
     * @param enumClass 枚举类
     * @param code 枚举code
     * @param defaultEnum 不存在时默认值
     * @return 枚举对象
     */
    static <E extends Enum<E>, T, R extends IStdEnum<T>> R codeOf(Class<E> enumClass, T code, R defaultEnum) {
        if (null == code) {
            return defaultEnum;
        }
        Map<?, IStdEnum<?>> stdEnumMap = StdEnumFactory.findFromCache(enumClass, IStdEnum::getCode);
        return (R) stdEnumMap.getOrDefault(code, defaultEnum);
    }

    class StdEnumFactory {

        private static final Map<Class<?>, Map<?, IStdEnum<?>>> cacheMap = new ConcurrentHashMap<>();

        public static <E extends Enum<E>> Map<?, IStdEnum<?>> findFromCache(Class<E> enumClass,
            Function<IStdEnum<?>, ?> keyFunction) {
            Objects.requireNonNull(enumClass, "enumClass is required!");
            Objects.requireNonNull(keyFunction, "keyFunction is required!");
            return cacheMap.computeIfAbsent(enumClass,
                key -> Collections.unmodifiableMap(loadEnumMap(enumClass, keyFunction)));
        }

        private static <E extends Enum<E>> Map<?, IStdEnum<?>> loadEnumMap(Class<E> enumClass,
            Function<IStdEnum<?>, ?> keyFunction) {
            Map<Object, IStdEnum<?>> result = new HashMap<>();
            EnumSet.allOf(enumClass).forEach(item -> {
                if (item instanceof IStdEnum) {
                    IStdEnum<?> stdEnum = (IStdEnum<?>) item;
                    result.put(keyFunction.apply(stdEnum), stdEnum);
                }
            });
            return result;
        }
    }

}
