package org.gy.framework.idempotent.core.support;

import org.aspectj.lang.JoinPoint;
import org.gy.framework.idempotent.annotation.Idempotent;
import org.gy.framework.lock.utils.InvokeUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 函数计算Key解析器
 *
 * @author gy
 */
@Component
public class FunctionIdempotentKeyResolver extends AbstractIdempotentKeyResolver {
    @Override
    protected String internalKeyExtractor(JoinPoint joinPoint, Idempotent idempotent) {
        Assert.hasText(idempotent.keyFunction(), () -> "Idempotent keyFunction must not be empty");
        Object result = InvokeUtils.invokeFunction(idempotent.keyFunction(), joinPoint);
        Assert.notNull(result, () -> "Idempotent keyFunction result is null");
        return result.toString();
    }
}
