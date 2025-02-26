package org.gy.framework.idempotent.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.gy.framework.core.util.JsonUtils;
import org.gy.framework.idempotent.annotation.Idempotent;
import org.gy.framework.lock.core.DistributedLock;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 幂等上下文
 *
 * @author gy
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class IdempotentContext implements Serializable {
    private static final long serialVersionUID = -5449143519694471827L;

    /**
     * 业务唯一标识定义
     */
    private String key;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 幂等的超时时间，默认为 1 秒
     */
    private int timeout = 1;

    /**
     * 时间单位，默认为 SECONDS 秒
     */
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    /**
     * 删除 Key，当发生异常时候
     */
    private boolean deleteKeyWhenException = true;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 锁服务
     */
    private transient DistributedLock lockService;
    /**
     * Idempotent注解
     */
    private transient Idempotent annotation;

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }
}
