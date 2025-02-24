package org.gy.framework.idempotent.core.support;

import org.aspectj.lang.JoinPoint;
import org.gy.framework.idempotent.annotation.Idempotent;

/**
 * 默认（全局级别）幂等 Key 解析器
 *
 * @author gy
 */
public class DefaultIdempotentKeyResolver extends AbstractIdempotentKeyResolver {

    @Override
    protected String internalKeyExtractor(JoinPoint joinPoint, Idempotent idempotent) {
        return paramKeyBuilder(joinPoint, null);
    }

}
