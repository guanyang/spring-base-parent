package org.gy.framework.limit.core.support;

import org.aspectj.lang.JoinPoint;
import org.gy.framework.limit.annotation.LimitCheck;
import org.springframework.stereotype.Component;

/**
 * 全局级别限流 Key 解析器
 */
@Component
public class GlobalLimitKeyResolver extends AbstractLimitKeyResolver {

    @Override
    protected String internalKeyExtractor(JoinPoint joinPoint, LimitCheck annotation) {
        return paramKeyBuilder(joinPoint, null);
    }
}
