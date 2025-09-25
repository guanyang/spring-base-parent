package io.github.guanyang.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import io.github.guanyang.core.dto.BaseResponse;
import io.github.guanyang.core.dto.PageResponse;
import io.github.guanyang.core.dto.Response;
import io.github.guanyang.util.data.BeanUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class BeanUtilTest {

    private Response<Integer> sourceBean;

    private Map<String, Object> sourceMap;

    private Integer sourceData;

    public static final String DATA_KEY = "data";

    @BeforeEach
    public void init() {
        sourceData = 100;
        sourceBean = Response.asSuccess(sourceData);
        sourceMap = BeanUtil.beanToMap(sourceBean);
    }

    @AfterEach
    public void destroy() {
        sourceData = null;
        sourceBean = null;
        sourceMap = null;
    }

    @Test
    public void beanToMapTest() {
        Map<String, Object> targetMap = BeanUtil.beanToMap(sourceBean);
        assertEquals(sourceData, targetMap.get(DATA_KEY));
    }


    @Test
    public void mapToBeanTest() {
        Response<Integer> response = BeanUtil.mapToBean(sourceMap, Response.class);
        assertEquals(sourceData, response.getData());
    }

    @Test
    public void copyListTest() {
        List<Response<Integer>> list = Lists.newArrayList(sourceBean, sourceBean, sourceBean);
        List<BaseResponse> result = BeanUtil.copyList(list, PageResponse.class).collect(Collectors.toList());
        assertEquals(3, result.size());
    }

}
