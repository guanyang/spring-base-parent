package org.gy.framework.util.data;

import com.google.common.collect.Maps;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * 功能描述：属性转换工具类
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class BeanUtil {

    private BeanUtil() {
    }

    /**
     * 将javabean对象转换为map
     */
    public static <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = Maps.newHashMap();
        if (bean == null) {
            return map;
        }
        try {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                Object value = beanMap.get(key);
                map.put(String.valueOf(key), value);
            }
        } catch (Exception e) {
            log.error("beanToMap exception,bean={}.", bean, e);
        }
        return map;
    }

    /**
     * 将map转换为javabean对象
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> type) {
        if (type == null || ObjectUtils.isEmpty(map)) {
            return null;
        }
        try {
            T bean = type.newInstance();
            BeanMap beanMap = BeanMap.create(bean);
            beanMap.putAll(map);
            return bean;
        } catch (Exception e) {
            log.error("mapToBean exception,class={}.", type.getName(), e);
            return null;
        }
    }

    public static <T, R> Stream<R> copyList(List<T> sourceList, Class<R> clazz) {
        return copyList(sourceList, clazz, t -> true);
    }

    public static <T, R> Stream<R> copyList(List<T> sourceList, Class<R> clazz, Predicate<Object> propertiesPredicate) {
        if (clazz == null || ObjectUtils.isEmpty(sourceList)) {
            return Stream.empty();
        }
        return sourceList.stream().map(s -> copyProperties(s, clazz, propertiesPredicate)).filter(Objects::nonNull);
    }

    public static <T, R> R copyProperties(T source, Class<R> clazz, Predicate<Object> propertiesPredicate) {
        try {
            R target = clazz.newInstance();
            copyProperties(source, target, propertiesPredicate);
            return target;
        } catch (Exception e) {
            log.error("copyProperties execption,source={},class={}.", source, clazz.getName(), e);
            return null;
        }
    }

    public static void copyProperties(Object source, Object target) {
        copyProperties(source, target, t -> true);
    }

    public static void copyProperties(Object source, Object target, Predicate<Object> propertiesPredicate) {
        copyProperties(source, target, propertiesPredicate, (String[]) null);
    }

    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        copyProperties(source, target, t -> true, ignoreProperties);
    }

    public static void copyProperties(Object source, Object target, Predicate<Object> propertiesPredicate,
        String... ignoreProperties) {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        Class<?> actualEditable = target.getClass();
        PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(actualEditable);
        List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : null);

        for (PropertyDescriptor targetPd : targetPds) {
            Method writeMethod = targetPd.getWriteMethod();
            if (writeMethod != null && (ignoreList == null || !ignoreList.contains(targetPd.getName()))) {
                PropertyDescriptor sourcePd = BeanUtils.getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (sourcePd != null) {
                    Method readMethod = sourcePd.getReadMethod();
                    if (readMethod != null && ClassUtils.isAssignable(writeMethod.getParameterTypes()[0],
                        readMethod.getReturnType())) {
                        try {
                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                readMethod.setAccessible(true);
                            }
                            Object value = readMethod.invoke(source);
                            Boolean valuePredicate = Optional.ofNullable(propertiesPredicate).map(p -> p.test(value))
                                .orElse(Boolean.TRUE);
                            if (valuePredicate) {
                                if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                    writeMethod.setAccessible(true);
                                }
                                writeMethod.invoke(target, value);
                            }
                        } catch (Throwable ex) {
                            throw new FatalBeanException(
                                "Could not copy property '" + targetPd.getName() + "' from source to target", ex);
                        }
                    }
                }
            }
        }
    }
}
