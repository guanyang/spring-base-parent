package org.gy.framework.core.filter;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public interface Filter<T, R> {

    /**
     * 功能描述：过滤执行
     *
     * @param request 过滤执行入参
     * @param response 过滤执行出参
     * @param chain 过滤链
     * @author gy
     * @version 1.0.0
     */
    void doFilter(FilterRequest<T> request, FilterResponse<R> response, FilterChain chain);

}
