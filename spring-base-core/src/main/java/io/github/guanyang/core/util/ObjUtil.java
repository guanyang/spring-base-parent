package io.github.guanyang.core.util;

import lombok.SneakyThrows;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.guanyang.core.util.StringUtil.hasText;

/**
 * @author gy
 */
public class ObjUtil {

    @SneakyThrows
    public static void trimStringFields(Object obj) {
        if (obj == null) {
            return;
        }
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == String.class) {
                // 检查字段是否为 final，否则反射报错
                if (Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                String value = (String) field.get(obj);
                if (value != null) {
                    field.set(obj, value.trim());
                }
            }
        }
    }


    public static <T> Map<T, Integer> merge(Map<T, Integer> map) {
        if (CollectionUtils.isEmpty(map)) {
            return Collections.emptyMap();
        }
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum, LinkedHashMap::new));
    }

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof Optional) {
            return !((Optional<?>) obj).isPresent();
        } else if (obj instanceof CharSequence) {
            return !hasText((CharSequence) obj);
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        } else if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        } else {
            return obj instanceof Map ? ((Map<?, ?>) obj).isEmpty() : false;
        }
    }
}
