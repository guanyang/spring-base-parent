package org.gy.framework.core.spi;

import static org.gy.framework.core.exception.Assert.hasText;

import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * 服务扩展工厂
 *
 * @author gy
 * @version 1.0.0
 */
public final class SpiExtensionFactory {

    private static final String DELIMITER = "@";
    private static final Map<Class<?>, ServiceLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Object>> CACHED_INSTANCES = new ConcurrentHashMap<>();


    public static <T extends SpiIdentity> T getExtension(String type, Class<T> clazz) {
        Map<String, Object> instances = getCachedInstances(clazz);
        String key = buildKey(type, clazz);
        return (T) instances.get(key);
    }

    public static <T extends SpiIdentity> void addExtension(Class<T> clazz, T service) {
        Map<String, Object> instances = getCachedInstances(clazz);
        serviceConsumer(clazz, service, instances::put);
    }

    public static <T extends SpiIdentity> void addExtensionIfAbsent(Class<T> clazz, T service) {
        Map<String, Object> instances = getCachedInstances(clazz);
        serviceConsumer(clazz, service, instances::putIfAbsent);
    }

    private static <T extends SpiIdentity> Map<String, Object> getCachedInstances(final Class<T> clazz) {
        Objects.requireNonNull(clazz, "extension clazz is null");
        return CACHED_INSTANCES.computeIfAbsent(clazz, key -> loadInstances(clazz));
    }


    public static <T extends SpiIdentity> ServiceLoader<T> getExtensionLoader(final Class<T> clazz) {

        Objects.requireNonNull(clazz, "extension clazz is null");

        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("extension clazz (" + clazz + ") is not interface!");
        }
        ServiceLoader<T> loader = (ServiceLoader<T>) EXTENSION_LOADERS.get(clazz);
        return (ServiceLoader<T>) EXTENSION_LOADERS.computeIfAbsent(clazz, key -> ServiceLoader.load(clazz));
    }

    private static <T extends SpiIdentity> Map<String, Object> loadInstances(final Class<T> clazz) {
        Map<String, Object> result = new ConcurrentHashMap<>();
        ServiceLoader<T> loader = getExtensionLoader(clazz);
        loader.forEach(s -> {
            serviceConsumer(clazz, s, result::put);
        });
        return result;
    }

    private static <T extends SpiIdentity> void serviceConsumer(Class<T> clazz, T service,
        BiConsumer<String, T> consumer) {
        String type = service.type();
        hasText(type, "SpiIdentity type is required:" + service);
        String key = buildKey(type, clazz);
        consumer.accept(key, service);
    }

    private static <T extends SpiIdentity> String buildKey(String type, Class<T> clazz) {
        return String.join(DELIMITER, clazz.getName(), type);
    }

}
