package io.github.guanyang.limit.core.support;

import org.aspectj.lang.JoinPoint;
import io.github.guanyang.limit.annotation.LimitCheck;
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
