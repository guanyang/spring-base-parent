package org.gy.framework.lock.core.support;

import org.aspectj.lang.JoinPoint;
import org.gy.framework.lock.annotation.Lock;
import org.gy.framework.lock.utils.InvokeUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 函数计算Key解析器
 *
 * @author gy
 */
@Component
public class FunctionLockKeyResolver extends AbstractLockKeyResolver {
    @Override
    protected String internalKeyExtractor(JoinPoint joinPoint, Lock annotation) {
        Assert.hasText(annotation.keyFunction(), () -> "Lock keyFunction must not be empty");
        Object result = InvokeUtils.invokeFunction(annotation.keyFunction(), joinPoint);
        Assert.notNull(result, () -> "Lock keyFunction result is null");
        return result.toString();
    }
}
