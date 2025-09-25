package io.github.guanyang.mq.core.support;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import io.github.guanyang.core.support.CommonServiceManager;
import io.github.guanyang.mq.core.EventAnnotationMethodProcessor;
import io.github.guanyang.mq.core.EventLogService;
import io.github.guanyang.mq.core.EventMessageConsumerService;
import io.github.guanyang.mq.model.DynamicEventContext;
import io.github.guanyang.mq.model.EventLogContext;
import io.github.guanyang.mq.model.EventMessage;
import io.github.guanyang.mq.model.IEventType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.MethodIntrospector.MetadataLookup;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public abstract class AbstractEventAnnotationMethodProcessor<A extends Annotation> implements EventAnnotationMethodProcessor<A> {

    protected final ObjectProvider<EventLogService> eventLogService;

    public AbstractEventAnnotationMethodProcessor(ObjectProvider<EventLogService> eventLogService) {
        this.eventLogService = eventLogService;
    }

    /**
     * 获取事件类型码
     *
     * @param annotation 注解
     * @return 事件类型码
     */
    protected abstract String getEventTypeCode(A annotation);

    /**
     * 构建事件上下文
     *
     * @param bean       bean
     * @param method     方法
     * @param annotation 注解
     * @return 事件上下文
     */
    protected abstract void eventContextCustomizer(DynamicEventContext<Object, Object> ctx, Method method, A annotation);

    /**
     * 查找指定bean中带注解的方法，并注册事件Bean
     *
     * @param bean     bean
     * @param beanName bean名称
     * @return 注册的事件Bean
     */
    @Override
    public Map<String, Object> registerAnnotationMethod(Object bean, String beanName) {
        return internalRegisterAnnotationMethod(bean, beanName);
    }

    /**
     * 执行方法
     *
     * @param invocation 方法调用
     * @return 方法返回值
     * @throws Throwable 抛出异常
     */
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        //如果事件激活，则直接执行
        boolean currentServiceActive = EventMessageServiceManager.isCurrentServiceActive();
        return currentServiceActive ? invocation.proceed() : internalInvoke(invocation);
    }

    /**
     * 内部执行方法
     *
     * @param invocation 方法调用
     * @return 方法返回值
     * @throws Throwable 抛出异常
     */
    protected Object internalInvoke(MethodInvocation invocation) throws Throwable {
        IEventType eventType = getEventType(invocation, this::getEventTypeCode);
        EventMessage<Object> req = buildEventMessage(eventType, invocation);
        return EventLogContext.handleWithLog(req, data -> proceed(invocation), ctx -> {
            EventLogService logService = eventLogService.getIfAvailable(DefaultEventLogServiceImpl::new);
            logService.batchSaveEventLog(ctx);
        });
    }

    /**
     * 执行方法
     *
     * @param invocation 方法调用
     * @return 方法返回值
     * @throws Throwable 抛出异常
     */
    @SneakyThrows
    protected Object proceed(MethodInvocation invocation) {
        return invocation.proceed();
    }

    /**
     * 获取事件类型
     *
     * @param invocation        方法调用
     * @param eventTypeFunction 事件类型函数
     * @return 事件类型
     */
    protected IEventType getEventType(MethodInvocation invocation, Function<A, String> eventTypeFunction) {
        Method method = invocation.getMethod();
        A annotation = method.getAnnotation(getAnnotationClass());
        String eventTypeCode = eventTypeFunction.apply(annotation);
        IEventType eventType = CommonServiceManager.getServiceOptional(IEventType.class, eventTypeCode).orElse(null);
        Assert.notNull(eventType, () -> "IEventType code not registered: " + eventTypeCode);
        return eventType;
    }

    /**
     * 构建事件消息
     *
     * @param eventType  事件类型
     * @param invocation 方法调用
     * @return 事件消息
     */
    protected EventMessage<Object> buildEventMessage(IEventType eventType, MethodInvocation invocation) {
        Assert.notNull(eventType, () -> "IEventType must not be null");
        Assert.notNull(invocation, () -> "MethodInvocation must not be null");
        Object[] args = invocation.getArguments();
        if (ArrayUtils.isEmpty(args) || args.length != 1) {
            throw new IllegalArgumentException("EventAnnotationMethodProcessor method only support one argument: " + eventType.getCode());
        }
        return EventMessage.of(eventType, args[0]);
    }


    /**
     * 查找指定bean中带注解的方法，并注册事件Bean
     *
     * @param bean     bean
     * @param beanName bean名称
     * @return 注册的事件Bean
     */
    protected Map<String, Object> internalRegisterAnnotationMethod(Object bean, String beanName) {
        Map<Method, A> methodMap = findAnnotationMethod(beanName, bean.getClass());
        if (MapUtil.isEmpty(methodMap)) {
            return Collections.emptyMap();
        }
        Map<String, Object> beanMap = Maps.newLinkedHashMap();
        for (Map.Entry<Method, A> entry : methodMap.entrySet()) {
            Method method = entry.getKey();
            A annotation = entry.getValue();
            DynamicEventContext<Object, Object> ctx = buildEventContext(bean, method, annotation);
            // 注册事件Bean
            EventMessageConsumerService<Object, Object> eventService = new DefaultDynamicEventMessageServiceImpl(ctx);
            String eventBeanName = uniqueKey(beanName, method.getName());
            SpringUtil.registerBean(eventBeanName, eventService);
            beanMap.put(eventBeanName, SpringUtil.getBean(eventBeanName));
        }
        return beanMap;
    }


    /**
     * 构建动态事件上下文
     *
     * @param bean       bean
     * @param method     方法
     * @param annotation 注解
     * @return 动态事件上下文
     */
    protected DynamicEventContext<Object, Object> buildEventContext(Object bean, Method method, A annotation) {
        Class<?> clazz = bean.getClass();
        String methodName = method.getName();
        Type[] paramTypes = method.getGenericParameterTypes();
        if (ArrayUtils.isEmpty(paramTypes) || paramTypes.length != 1) {
            throw new IllegalStateException("EventAnnotationMethodProcessor method invalid, only one param support, for[" + clazz + "#" + methodName + "].");
        }
        method.setAccessible(true);
        DynamicEventContext<Object, Object> ctx = new DynamicEventContext<>();
        Function<Object, Object> executeFunction = data -> ReflectionUtils.invokeMethod(method, bean, data);
        ctx.setExecuteFunction(executeFunction);
        ctx.setDataTypeReference(paramTypes[0]);
        // 自定义处理
        eventContextCustomizer(ctx, method, annotation);
        ctx.contextCheck();
        return ctx;
    }

    /**
     * 寻找指定类中带注解的方法，并返回方法名和注解的映射关系
     *
     * @param beanName  beanName
     * @param beanClass beanClass
     * @return 方法名和注解的映射关系
     */
    protected Map<Method, A> findAnnotationMethod(String beanName, Class<?> beanClass) {
        try {
            return MethodIntrospector.selectMethods(beanClass, (MetadataLookup<A>) method -> AnnotatedElementUtils.findMergedAnnotation(method, getAnnotationClass()));
        } catch (Throwable ex) {
            log.warn("EventAnnotationMethodProcessor findAnnotationMethod error for bean[{}].", beanName, ex);
            return Collections.emptyMap();
        }
    }

    public static String uniqueKey(Object... keys) {
        return StringUtils.joinWith(StrUtil.UNDERLINE, keys);
    }
}
