package io.github.guanyang.core.support;

import org.springframework.core.Ordered;

/**
 * <p>
 * 通用Service层Action接口
 * </p>
 */
public interface CommonServiceAction extends Ordered {

    default void init() {
    }

    default void destroy() {
    }

    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
