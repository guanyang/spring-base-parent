package org.gy.framework.lock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author guanyang
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LockEntry implements Serializable {
    private static final long serialVersionUID = -3530685587448063245L;
    /**
     * 锁key
     */
    private String lockKey;
    /**
     * 过期时间，单位：毫秒
     */
    private long expireMillis;
    /**
     * 是否自动续期，默认否
     */
    private boolean renewal = false;
}
