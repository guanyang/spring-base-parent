package io.github.guanyang.limit.core;

import org.aspectj.lang.JoinPoint;
import io.github.guanyang.core.spi.SpiExtensionFactory;
import io.github.guanyang.limit.annotation.LimitCheck;
import io.github.guanyang.limit.model.LimitCheckContext;
import io.github.guanyang.limit.enums.LimitTypeEnum;
import io.github.guanyang.limit.exception.LimitException;

/**
 * 频率限制检查分发定义
 *
 * @author gy
 * @version 1.0.0
 */
public interface ILimitCheckServiceDispatch {

    default ILimitCheckService findService(LimitTypeEnum typeEnum) {
        return findService(typeEnum.getCode());
    }

    default ILimitCheckService findService(String type) {
        return SpiExtensionFactory.getExtension(type, ILimitCheckService.class);
    }

    LimitCheckContext createContext(JoinPoint joinPoint, LimitCheck check);

    boolean check(LimitCheckContext checkContext);

    /**
     * 执行fallback降级函数
     */
    Object invokeFallback(JoinPoint joinPoint, LimitCheck limitCheck, LimitException limitException);

}
