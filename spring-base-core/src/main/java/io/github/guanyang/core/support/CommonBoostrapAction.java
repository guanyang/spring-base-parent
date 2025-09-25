package io.github.guanyang.core.support;

import org.springframework.core.Ordered;

/**
 * <p>
 * 通用启动器Action接口
 * </p>
 */
public interface CommonBoostrapAction extends Ordered {

    default void init() {
    }

    default void destroy() {
    }

    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
