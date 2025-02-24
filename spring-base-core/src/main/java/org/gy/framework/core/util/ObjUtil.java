package org.gy.framework.core.util;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Buck
 * @Date 2023/9/14 19:08
 * @Desc
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
}
