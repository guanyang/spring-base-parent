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

    private final List<Filter> filters = new ArrayList<>();

    private int currentPosition = 0;

    public DefaultFilterChain addFilter(Filter filter) {
        filters.add(filter);
        return this;
    }

    public DefaultFilterChain addFilter(List<? extends Filter> filterList) {
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
