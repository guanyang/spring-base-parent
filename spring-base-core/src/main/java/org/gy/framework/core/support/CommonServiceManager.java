package org.gy.framework.core.support;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.exception.CommonException;
import org.gy.framework.core.util.CollectionUtils;
import org.gy.framework.core.util.ConcurrentLinkedHashMap;
import org.gy.framework.core.util.ConcurrentLinkedHashMap.LockType;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.gy.framework.core.exception.CommonErrorCode.PARAM_BAD_REQUEST;

@Slf4j
@Component
public class CommonServiceManager implements CommonBoostrapAction {

    private static final Map<Class<? extends CommonServiceAction>, Map<Object, CommonServiceAction>> SERVICE_MAP = new ConcurrentHashMap<>();

    private final AtomicBoolean init = new AtomicBoolean(false);

    //延迟查询，可以获取所有bean，包括动态注册的Bean
    private Map<String, CommonServiceAction> actionMap;

    public static <S extends CommonServiceAction, K> S getService(TypeReference<S> typeRef, K key) {
        Assert.notNull(typeRef, () -> new CommonException(PARAM_BAD_REQUEST));
        Assert.notNull(key, () -> new CommonException(PARAM_BAD_REQUEST));

        Object service = Optional.ofNullable(SERVICE_MAP.get(TypeUtil.getClass(typeRef))).map(map -> map.get(key)).orElse(null);
        return (S) Assert.notNull(service, () -> new CommonException(PARAM_BAD_REQUEST));
    }

    public static <S extends CommonServiceAction, K> Optional<S> getServiceOptional(TypeReference<S> typeRef, K key) {
        return Optional.ofNullable(typeRef).map(t -> SERVICE_MAP.get(TypeUtil.getClass(t))).map(map -> (S) map.get(key));
    }

    public static <S extends CommonServiceAction, K> S getService(Class<S> serviceClass, K key) {
        Assert.notNull(serviceClass, () -> new CommonException(PARAM_BAD_REQUEST));
        Assert.notNull(key, () -> new CommonException(PARAM_BAD_REQUEST));

        Object service = Optional.ofNullable(SERVICE_MAP.get(serviceClass)).map(map -> map.get(key)).orElse(null);
        return (S) Assert.notNull(service, () -> new CommonException(PARAM_BAD_REQUEST));
    }

    public static <S extends CommonServiceAction, K> Optional<S> getServiceOptional(Class<S> serviceClass, K key) {
        return Optional.ofNullable(SERVICE_MAP.get(serviceClass)).map(map -> (S) map.get(key));
    }

    public static <S extends CommonServiceAction, K> Map<K, S> getService(TypeReference<S> typeRef) {
        Assert.notNull(typeRef, () -> new CommonException(PARAM_BAD_REQUEST));
        Map<K, S> ksMap = (Map<K, S>) Optional.ofNullable(SERVICE_MAP.get(TypeUtil.getClass(typeRef))).orElseGet(Collections::emptyMap);
        return Collections.unmodifiableMap(ksMap);
    }

    public static <S extends CommonServiceAction, K> Map<K, S> getService(Class<S> serviceClass) {
        Assert.notNull(serviceClass, () -> new CommonException(PARAM_BAD_REQUEST));
        Map<K, S> ksMap = (Map<K, S>) Optional.ofNullable(SERVICE_MAP.get(serviceClass)).orElseGet(Collections::emptyMap);
        return Collections.unmodifiableMap(ksMap);
    }

    public static <S extends CommonServiceAction, K> void register(Class<S> serviceClass, Collection<S> serviceInstances, Function<? super S, ? extends K> keyMapper) {
        if (CollectionUtil.isEmpty(serviceInstances)) {
            log.warn("[CommonServiceManager]Param serviceList is empty");
            return;
        }
        serviceInstances.forEach(service -> registerInstance(serviceClass, service, keyMapper));
    }

    public static <S extends CommonServiceAction, K> void registerInstance(Class<S> serviceClass, S serviceInstance, Function<? super S, ? extends K> keyMapper) {
        Assert.notNull(serviceInstance, "CommonServiceManager serviceInstance is null");
        Assert.notNull(keyMapper, "CommonServiceManager keyMapper is null");
        Assert.notNull(serviceClass, "CommonServiceManager serviceClass is null");


        String className = serviceClass.getName();
        K key = Assert.notNull(keyMapper.apply(serviceInstance), "CommonServiceManager key is null: {}", className);

        Map<Object, CommonServiceAction> serviceInstanceMap = SERVICE_MAP.computeIfAbsent(serviceClass, k -> new ConcurrentLinkedHashMap<>(LockType.StampedLock));
        //禁止注册相同的类型，避免覆盖导致业务错误
        Assert.isFalse(serviceInstanceMap.containsKey(key), "CommonServiceManager service already registered: service={}, key={}", serviceClass.getName(), key);

        serviceInstanceMap.put(key, serviceInstance);
    }

    @Override
    public void init() {
        actionMap = SpringUtil.getBeansOfType(CommonServiceAction.class);
        if (CollectionUtils.isEmpty(actionMap)) {
            log.warn("[CommonServiceManager]no service action");
            return;
        }
        if (init.compareAndSet(false, true)) {
            actionMap.values().stream().sorted(Comparator.comparingInt(CommonServiceAction::getOrder)).forEach(CommonServiceAction::init);
            log.info("[CommonServiceManager]init success, service size: {}", actionMap.size());
        }
    }

    @Override
    public void destroy() {
        SERVICE_MAP.clear();
        if (init.compareAndSet(true, false)) {
            actionMap.values().stream().sorted(Comparator.comparingInt(CommonServiceAction::getOrder).reversed()).forEach(CommonServiceAction::destroy);
            log.info("[CommonServiceManager]destroy success, service size: {}", actionMap.size());
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
