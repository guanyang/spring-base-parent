package org.gy.framework.core.support;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.util.CollectionUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class CommonBoostrapManager implements SmartInitializingSingleton, DisposableBean, Ordered {

    private final AtomicBoolean init = new AtomicBoolean(false);

    //延迟查询，可以获取所有bean，包括动态注册的Bean
    private Map<String, CommonBoostrapAction> actionMap;

    @Override
    public void destroy() {
        if (init.compareAndSet(true, false)) {
            actionMap.values().stream().sorted(Comparator.comparingInt(CommonBoostrapAction::getOrder).reversed()).forEach(CommonBoostrapAction::destroy);
            log.info("[CommonBoostrapManager]destroy success, service size: {}", actionMap.size());
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        actionMap = SpringUtil.getBeansOfType(CommonBoostrapAction.class);
        if (CollectionUtils.isEmpty(actionMap)) {
            log.warn("[CommonBoostrapManager]no boostrap action");
            return;
        }
        if (init.compareAndSet(false, true)) {
            actionMap.values().stream().sorted(Comparator.comparingInt(CommonBoostrapAction::getOrder)).forEach(CommonBoostrapAction::init);
            log.info("[CommonBoostrapManager]init success, service size: {}", actionMap.size());
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
