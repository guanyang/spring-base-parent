package io.github.guanyang.limit.annotation;

import io.github.guanyang.limit.core.ILimitCheckService;
import io.github.guanyang.limit.core.LimitKeyResolver;
import io.github.guanyang.limit.core.support.*;
import io.github.guanyang.limit.enums.LimitTypeEnum;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 执行频率限制注解
 *
 * @author gy
 * @version 1.0.0
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LimitCheck {

    /**
     * 提示信息
     */
    String message() default "Too Many Requests";

    /**
     * 使用的 Key 解析器
     *
     * @see GlobalLimitKeyResolver 全局级别
     * @see ClientIpLimitKeyResolver 客户端IP
     * @see ServerNodeLimitKeyResolver 服务器节点
     * @see ExpressionLimitKeyResolver 自定义表达式，通过 {@link #key()} 计算
     * @see FunctionLimitKeyResolver 自定义函数，通过 {@link #keyFunction()} 计算
     */
    Class<? extends LimitKeyResolver> keyResolver() default ExpressionLimitKeyResolver.class;

    /**
     * 频率限制key
     */
    String key() default "";

    /**
     * Key计算函数
     */
    String keyFunction() default "";

    /**
     * 限制阈值<br>
     * <li>时间窗口模式表示请求阈值</li>
     * <li>令牌桶模式表示令牌生产速率</li>
     * <li>滑动窗口模式表示请求阈值</li>
     */
    int limit() default 1;

    /**
     * 限制阈值表达式，支持SpEL或${spring.xxx}，优先级高于limit
     */
    String limitExpression() default "";

    /**
     * 限制时间，默认为 60s（时间窗口、滑动窗口模式有效）
     */
    int time() default 60;

    /**
     * 时间单位，默认为 SECONDS 秒（时间窗口、滑动窗口模式有效）
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * key前缀
     */
    String keyPrefix() default "limitCheck";

    /**
     * 限流模式，支持SPI扩展，默认redis时间窗口模式<br>
     * <li>TIME_WINDOW: redis时间窗口模式</li>
     * <li>TOKEN_BUCKET: redis令牌桶模式</li>
     * <li>SLIDING_WINDOW: redis滑动窗口模式</li>
     *
     * @see LimitTypeEnum 模式定义枚举
     * @see ILimitCheckService#type() SPI扩展点
     */
    String type() default "";

    /**
     * 限流模式枚举，type优先级高于typeEnum<br>
     */
    LimitTypeEnum typeEnum() default LimitTypeEnum.TIME_WINDOW;

    /**
     * 用于令牌桶模式，表示令牌桶的桶的大小，这个参数控制了请求最大并发数
     */
    int capacity() default 1;

    /**
     * 令牌桶容量表达式，支持SpEL或${spring.xxx}，优先级高于capacity
     */
    String capacityExpression() default "";

    /**
     * 用于令牌桶模式，表示每次获取的令牌数，一般不用改动这个参数值
     */
    int requested() default 1;

    /**
     * 限流后的自定义降级方法，不定义则抛出LimitException
     */
    String fallback() default "";

    /**
     * 降级方法所在的 Spring Bean，默认为当前类
     */
    Class<?> fallbackBean() default Void.class;

}
