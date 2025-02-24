package org.gy.framework.limit.core.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import org.aspectj.lang.JoinPoint;
import org.gy.framework.limit.annotation.LimitCheck;
import org.springframework.stereotype.Component;

/**
 * 服务器节点级别限流key解析器
 *
 * @author gy
 */
@Component
public class ServerNodeLimitKeyResolver extends AbstractLimitKeyResolver {
    @Override
    protected String internalKeyExtractor(JoinPoint joinPoint, LimitCheck annotation) {
        String serverNode = StrUtil.join(StrUtil.AT, SystemUtil.getHostInfo().getAddress(), SystemUtil.getCurrentPID());
        return paramKeyBuilder(joinPoint, k -> k.append(serverNode));
    }
}
