package io.github.guanyang.log.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TraceRequest<T> {

    /**
     * 当前执行类
     */
    private Class<?> executeClazz;
    /**
     * 当前执行方法
     */
    private String executeMethodName;
    /**
     * 当前请求参数
     */
    private T requestObj;
    /**
     * 操作描述
     */
    private String desc;
    /**
     * 是否捕获请求参数，默认true
     */
    private boolean requestBodyTrace = true;
    /**
     * 是否捕获返回参数，默认true
     */
    private boolean responseBodyTrace = true;

}
