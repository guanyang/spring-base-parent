package org.gy.framework.limit.core.support;

import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.spi.SpiExtensionFactory;
import org.gy.framework.limit.core.ILimitCheckService;
import org.gy.framework.limit.core.ILimitCheckServiceDispatch;

/**
 * 频率限制检查服务工厂
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class DefaultLimitCheckServiceDispatch implements ILimitCheckServiceDispatch {

    public static void addLimitCheckIfAbsent(ILimitCheckService service) {
        SpiExtensionFactory.addExtensionIfAbsent(ILimitCheckService.class, service);
    }

    @Override
    public ILimitCheckService findService(String type) {
        return SpiExtensionFactory.getExtension(type, ILimitCheckService.class);
    }
}
