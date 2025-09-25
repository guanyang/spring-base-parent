package io.github.guanyang.limit.core.support;

import org.aspectj.lang.JoinPoint;
import io.github.guanyang.limit.annotation.LimitCheck;
import io.github.guanyang.limit.util.InvokeUtils;
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
