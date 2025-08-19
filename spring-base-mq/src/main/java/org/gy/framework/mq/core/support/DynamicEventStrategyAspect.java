package org.gy.framework.mq.core.support;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.gy.framework.core.support.CommonServiceManager;
import org.gy.framework.mq.annotation.DynamicEventStrategy;
import org.gy.framework.mq.core.EventLogService;
import org.gy.framework.mq.model.EventLogContext;
import org.gy.framework.mq.model.EventMessage;
import org.gy.framework.mq.model.IEventType;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

@Slf4j
@Aspect
public class DynamicEventStrategyAspect {

    private final EventLogService eventLogService;

    public DynamicEventStrategyAspect(EventLogService eventLogService) {
        this.eventLogService = eventLogService;
    }

    @Pointcut("@annotation(org.gy.framework.mq.annotation.DynamicEventStrategy)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) {
        boolean currentServiceActive = EventMessageServiceManager.isCurrentServiceActive();
        //如果事件激活(本身已经记录日志)，则直接执行
        if (currentServiceActive) {
            return proceed(joinPoint);
        }
        //获取方法
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getName();
        // 获取AspectAnnotation注解
        DynamicEventStrategy eventStrategy = method.getAnnotation(DynamicEventStrategy.class);
        String eventTypeCode = eventStrategy.eventTypeCode();
        IEventType eventType = CommonServiceManager.getServiceOptional(IEventType.class, eventTypeCode).orElse(null);
        Assert.notNull(eventType, () -> "IEventType code not support: " + eventTypeCode);
        log.info("[DynamicEventStrategyAspect][{}]接口日志处理：eventTypeCode={}", methodName, eventTypeCode);

        EventMessage<Object> req = buildEventSendReq(eventType, joinPoint);
        return EventLogContext.handleWithLog(req, data -> proceed(joinPoint), eventLogService::batchSaveEventLog);
    }

    @SneakyThrows
    protected Object proceed(ProceedingJoinPoint joinPoint) {
        return joinPoint.proceed(joinPoint.getArgs());
    }

    protected EventMessage<Object> buildEventSendReq(IEventType eventType, ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Object data = null;
        if (ArrayUtils.isNotEmpty(args)) {
            data = args[0];
        }
        return EventMessage.of(eventType, data);
    }
}
