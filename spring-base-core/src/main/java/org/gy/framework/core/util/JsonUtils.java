package org.gy.framework.core.util;

import static org.gy.framework.core.exception.Assert.hasContent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;
import java.lang.reflect.Type;
import lombok.extern.slf4j.Slf4j;

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
        if (hasContent(json)) {
            try {
                return GSON.fromJson(json, clazz);
            } catch (Throwable e) {
                log.error("json转object异常: json=[{}], class=[{}]", json, clazz, e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static <T> T toObject(Reader reader, Class<T> clazz) {
        if (null != reader) {
            try {
                return GSON.fromJson(reader, clazz);
            } catch (Throwable e) {
                log.error("json转object异常: , class=[{}]", clazz, e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static <T> T toObject(String json, Type type) {
        if (hasContent(json)) {
            try {
                return GSON.fromJson(json, type);
            } catch (Throwable e) {
                log.error("json转object异常: json=[{}], type=[{}]", json, type, e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static String toJson(Object obj) {
        try {
            return GSON.toJson(obj);
        } catch (Throwable e) {
            log.error("object转json异常: object=[{}]", obj, e);
            return null;
        }
    }

}
