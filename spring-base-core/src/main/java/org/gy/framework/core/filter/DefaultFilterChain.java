package org.gy.framework.core.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class DefaultFilterChain implements FilterChain {

    private List<Filter> filters;

    private int currentPosition = 0;

    public DefaultFilterChain(List<Filter> filters) {
        this.filters = filters == null ? new ArrayList<>() : filters;
    }

    public DefaultFilterChain addFilter(Filter filter) {
        filters.add(filter);
        return this;
    }

    public DefaultFilterChain addFilter(List<Filter> filterList) {
        filters.addAll(filterList);
        return this;
    }

    @Override
    public void doFilter(FilterRequest request, FilterResponse response) {
        if (currentPosition == filters.size() || response.isEndFlag()) {
            return;
        }
        Filter filter = filters.get(currentPosition++);
        filter.doFilter(request, response, this);
    }
}
