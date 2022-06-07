package org.gy.framework.limit.core.support;

import lombok.Data;
import lombok.experimental.Accessors;

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
     * 时间期限，单位：秒
     */
    private int time;
    /**
     * 限制阈值
     */
    private long limit;

    public static LimitCheckContext of(String key, int time, long limit) {
        LimitCheckContext ctx = new LimitCheckContext();
        ctx.setKey(key).setTime(time).setLimit(limit);
        return ctx;
    }

}
