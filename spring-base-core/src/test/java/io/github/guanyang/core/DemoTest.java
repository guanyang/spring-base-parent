package io.github.guanyang.core;

import java.util.List;
import java.util.UUID;
import org.assertj.core.util.Lists;
import io.github.guanyang.core.filter.DefaultFilterChain;
import io.github.guanyang.core.filter.Filter;
import io.github.guanyang.core.filter.FilterChain;
import io.github.guanyang.core.filter.FilterRequest;
import io.github.guanyang.core.filter.FilterResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class DemoTest {

    @Test
    public void test1() {
        List<Filter> filterList = Lists.newArrayList();
        filterList.add(new TestFilter1());
        filterList.add(new TestFilter2());

        DefaultFilterChain chain = new DefaultFilterChain(filterList);
        FilterRequest<String> request = new FilterRequest<>("request");
        FilterResponse<String> response = new FilterResponse<>("response");

        chain.doFilter(request, response);

        Assertions.assertEquals(2, request.getAttachments().size());
        Assertions.assertEquals(2, response.getAttachments().size());

    }


    public static class TestFilter1 implements Filter<String, String> {

        @Override
        public void doFilter(FilterRequest<String> request, FilterResponse<String> response, FilterChain<String,String> chain) {
            request.setAttachment("requestKey1", UUID.randomUUID().toString());
            response.setAttachment("responseKey1", UUID.randomUUID().toString());
            chain.doFilter(request, response);
        }
    }

    public static class TestFilter2 implements Filter<String, String> {

        @Override
        public void doFilter(FilterRequest<String> request, FilterResponse<String> response, FilterChain<String,String> chain) {
            request.setAttachment("requestKey2", UUID.randomUUID().toString());
            response.setAttachment("responseKey2", UUID.randomUUID().toString());
            chain.doFilter(request, response);
        }
    }
}
