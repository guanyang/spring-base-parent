package org.gy.framework.limit.core.support;

import org.aspectj.lang.JoinPoint;
import org.gy.framework.limit.annotation.LimitCheck;
import org.gy.framework.limit.util.InvokeUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 函数计算Key解析器
 *
 * @author gy
 */
@Component
public class FunctionLimitKeyResolver extends AbstractLimitKeyResolver {
    @Override
    protected String internalKeyExtractor(JoinPoint joinPoint, LimitCheck annotation) {
        Assert.hasText(annotation.keyFunction(), () -> "LimitCheck keyFunction must not be empty");
        Object result = InvokeUtils.invokeFunction(annotation.keyFunction(), joinPoint);
        Assert.notNull(result, () -> "LimitCheck keyFunction result is null");
        return result.toString();
    }
}
