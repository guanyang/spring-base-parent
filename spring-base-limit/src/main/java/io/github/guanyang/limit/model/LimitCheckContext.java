package io.github.guanyang.limit.model;

import lombok.Data;
import lombok.experimental.Accessors;
import io.github.guanyang.core.util.JsonUtils;
import io.github.guanyang.limit.core.ILimitCheckService;
import io.github.guanyang.limit.enums.LimitTypeEnum;

/**
 * 频率限制检查上下文
 *
 * @author gy
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class LimitCheckContext {

    /**
     * 业务唯一标识定义
     */
    private String key;
    /**
     * 提示信息
     */
    private String message;
    /**
     * 时间期限，单位：秒
     */
    @Deprecated
    private int time;
    /**
     * 时间期限，单位：毫秒
     */
    private long timeInMillis;
    /**
     * 限制阈值
     */
    private long limit;

    /**
     * 频率限制类型，支持自定义扩展，默认redis时间窗口模式
     *
     * @see LimitTypeEnum.REDIS redis时间窗口模式
     * @see LimitTypeEnum.REDIS_TOKEN_BUCKET redis令牌桶模式
     * @see ILimitCheckService#type() SPI扩展点
     */
    private String type;

    /**
     * 用于令牌桶模式，表示令牌桶的桶的大小，这个参数控制了请求最大并发数
     */
    private int capacity;

    /**
     * 用于令牌桶模式，表示每次获取的令牌数，一般不用改动这个参数值
     */
    private int requested;

    private transient ILimitCheckService checkService;

    private String methodName;

    @Deprecated
    public static LimitCheckContext of(String key, int time, long limit) {
        LimitCheckContext ctx = new LimitCheckContext();
        ctx.setKey(key).setTime(time).setLimit(limit);
        ctx.setTimeInMillis(time * 1000L);
        return ctx;
    }

    public static LimitCheckContext of(String key, long timeInMillis, long limit) {
        LimitCheckContext ctx = new LimitCheckContext();
        ctx.setKey(key).setTimeInMillis(timeInMillis).setLimit(limit);
        ctx.setTime((int) (timeInMillis / 1000L));
        return ctx;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }

}
