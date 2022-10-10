package org.gy.framework.core.util;

import static org.gy.framework.core.exception.Assert.hasContent;
import static org.gy.framework.core.exception.Assert.isEmpty;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 功能描述：转换工具类
 *
 * @author gy
 * @version 1.0.0
 * @date 2022/10/10 14:45
 */
public class ConvertHelper {

    private static final String DOT = ",";

    public static final Type SET_INT_TYPE = new TypeToken<Set<Integer>>() {
    }.getType();

    public static final Type SET_STRING_TYPE = new TypeToken<Set<String>>() {
    }.getType();

    public static final Type MAP_STRING_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    public static final Function<String, Set<Integer>> SET_INT_FUNCTION = content -> {
        return JsonUtils.toObject(content, SET_INT_TYPE);
    };

    public static final Function<String, Set<String>> SET_STRING_FUNCTION = content -> {
        return JsonUtils.toObject(content, SET_STRING_TYPE);
    };

    public static final Function<String, Map<String, String>> MAP_STRING_FUNCTION = content -> {
        return JsonUtils.toObject(content, MAP_STRING_TYPE);
    };

    public static final Function<String, String[]> STRING_SPLIT_DOT = s -> s.split(DOT);

    private ConvertHelper() {

    }

    public static <R> R stringConvert(String value, Function<String, R> mapper, Supplier<R> defaultValue) {
        return objectConvert(value, mapper, defaultValue);
    }

    public static <T, R> R objectConvert(T req, Function<T, R> mapper, Supplier<R> defaultValue) {
        return Optional.ofNullable(req).map(mapper).filter(Objects::nonNull).orElseGet(defaultValue);
    }

    public static <R> Set<R> convertToSet(String source, Function<String, R> mapper) {
        return convertToSet(source, mapper, Long.MAX_VALUE);
    }

    public static <R> Set<R> convertToSet(String source, Function<String, R> mapper, long maxSize) {
        return convertStream(source, mapper).filter(Objects::nonNull).limit(maxSize).collect(Collectors.toSet());
    }

    public static <R> List<R> convertToList(String source, Function<String, R> mapper) {
        return convertToList(source, mapper, Long.MAX_VALUE);
    }

    public static <R> List<R> convertToList(String source, Function<String, R> mapper, long maxSize) {
        return convertStream(source, mapper).filter(Objects::nonNull).limit(maxSize).collect(Collectors.toList());
    }

    public static <T, R> List<R> convertToList(List<T> source, Function<T, R> mapper) {
        return convertToList(source, mapper, Long.MAX_VALUE);
    }

    public static <T, R> List<R> convertToList(List<T> source, Function<T, R> mapper, long maxSize) {
        return convertStream(source, mapper).filter(Objects::nonNull).limit(maxSize).collect(Collectors.toList());
    }

    public static <T, R> Stream<R> convertStream(List<T> source, Function<T, R> mapper) {
        if (isEmpty(source)) {
            return Stream.empty();
        }
        return source.stream().map(mapper);
    }

    public static <R> Stream<R> convertStream(String source, Function<String, R> mapper) {
        return convertStream(source, STRING_SPLIT_DOT, mapper);
    }

    public static <R> Stream<R> convertStream(String source, Function<String, String[]> splitFunc,
        Function<String, R> mapper) {
        if (!hasContent(source)) {
            return Stream.empty();
        }
        return Stream.of(splitFunc.apply(source)).map(mapper);
    }

}
