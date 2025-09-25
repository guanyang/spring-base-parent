package io.github.guanyang.core.filter;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public interface FilterChain<T, R> {

    /**
     * 功能描述：过滤执行
     *
     * @param request 请求对象
     * @param response 响应对象
     * @author gy
     * @version 1.0.0
     */
    void doFilter(FilterRequest<T> request, FilterResponse<R> response);
}
