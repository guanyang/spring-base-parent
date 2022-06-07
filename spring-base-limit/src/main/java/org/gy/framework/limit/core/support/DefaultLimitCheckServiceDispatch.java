package org.gy.framework.limit.core.support;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import javax.imageio.spi.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.limit.core.ILimitCheckService;
import org.gy.framework.limit.core.ILimitCheckServiceDispatch;
import org.springframework.util.StringUtils;

/**
 * 频率限制检查服务工厂
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class DefaultLimitCheckServiceDispatch implements ILimitCheckServiceDispatch {

    private static final Map<String, ILimitCheckService> CHECK_SERVICE_MAP = new ConcurrentHashMap<>();

    static {
        Iterator<ILimitCheckService> providers = ServiceRegistry.lookupProviders(ILimitCheckService.class);
        while (providers.hasNext()) {
            ILimitCheckService service = providers.next();
            limitCheckServiceConsumer(service, CHECK_SERVICE_MAP::put);
        }
    }

    public static void addLimitCheck(ILimitCheckService service) {
        limitCheckServiceConsumer(service, CHECK_SERVICE_MAP::put);
    }

    public static void addLimitCheckIfAbsent(ILimitCheckService service) {
        limitCheckServiceConsumer(service, CHECK_SERVICE_MAP::putIfAbsent);
    }


    private static void limitCheckServiceConsumer(ILimitCheckService service,
        BiConsumer<String, ILimitCheckService> consumer) {
        String type = service.type();
        if (!StringUtils.hasText(type)) {
            throw new IllegalArgumentException("ILimitCheckService type is required:" + service);
        }
        consumer.accept(type, service);
    }

    @Override
    public ILimitCheckService findService(String type) {
        return CHECK_SERVICE_MAP.get(type);
    }
}
