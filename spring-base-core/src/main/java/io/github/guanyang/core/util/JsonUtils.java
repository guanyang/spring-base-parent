package io.github.guanyang.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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
            .registerTypeAdapterFactory(new LenientTypeAdapterFactory()) //跳过错误字段
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

    public static class LenientTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    try {
                        delegate.write(out, value);
                    } catch (Exception e) {
                        // 序列化时忽略错误字段
                        out.nullValue();
                    }
                }

                @Override
                public T read(JsonReader in) throws IOException {
                    try {
                        return delegate.read(in);
                    } catch (Exception e) {
                        // 反序列化时跳过错误字段
                        in.skipValue();
                        return null;
                    }
                }
            };
        }
    }

}
