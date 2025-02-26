package org.gy.framework.limit.core;

import org.aspectj.lang.JoinPoint;
import org.gy.framework.core.spi.SpiExtensionFactory;
import org.gy.framework.limit.annotation.LimitCheck;
import org.gy.framework.limit.model.LimitCheckContext;
import org.gy.framework.limit.enums.LimitTypeEnum;
import org.gy.framework.limit.exception.LimitException;

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
