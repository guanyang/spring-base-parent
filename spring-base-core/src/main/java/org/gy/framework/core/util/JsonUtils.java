package org.gy.framework.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class JsonUtils {

    private static final Gson GSON = new GsonBuilder()
            .disableInnerClassSerialization()           //禁此序列化内部类
            .disableHtmlEscaping()                      //禁止转义html标签
            .create();

    private JsonUtils() {

    }

    public static <T> T toObject(String json, Class<T> clazz) {
        return execute(json, clazz, GSON::fromJson);
    }

    public static <T> T toObject(Reader reader, Class<T> clazz) {
        return execute(reader, clazz, GSON::fromJson);
    }

    public static <T> T toObject(String json, Type type) {
        return execute(json, type, GSON::fromJson);
    }

    public static String toJson(Object obj) {
        return execute(obj, GSON::toJson);
    }

    private static <T, R> R execute(T t, Function<T, R> function) {
        try {
            return function.apply(t);
        } catch (Throwable e) {
            log.warn("[JsonUtils]execute error: t={}", t, e);
            return null;
        }
    }

    private static <T, U, R> R execute(T t, U u, BiFunction<T, U, R> function) {
        try {
            return function.apply(t, u);
        } catch (Throwable e) {
            log.warn("[JsonUtils]execute error: t={}, u={}", t, u, e);
            return null;
        }
    }

}
