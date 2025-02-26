package org.gy.framework.lock.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.gy.framework.core.util.JsonUtils;
import org.gy.framework.lock.annotation.Lock;
import org.gy.framework.lock.core.DistributedLock;

import java.io.Serializable;

/**
 * 分布式锁上下文
 *
 * @author gy
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class LockContext implements Serializable {
    private static final long serialVersionUID = -135026541136507758L;
    /**
     * 业务唯一标识定义
     */
    private String key;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 锁过期时间，单位：毫秒
     */
    private int expireTimeMillis;

    /**
     * 等待超时时间，单位：毫秒
     * <li>waitTimeMillis=0，仅尝试一次获取锁<li/>
     * <li>waitTimeMillis=-1，一直尝试获取锁直到成功<li/>
     * <li>waitTimeMillis>0，自定义阻塞时间，超时则获取锁失败<li/>
     */
    private long waitTimeMillis;

    /**
     * 睡眠重试时间，单位：毫秒
     */
    private long sleepTimeMillis;

    /**
     * 是否自动续期，默认否
     */
    private boolean renewal;
    /**
     * 方法名
     */
    private String methodName;

    /**
     * 锁服务
     */
    private transient DistributedLock lockService;
    /**
     * lock注解
     */
    private transient Lock annotation;

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }
}
