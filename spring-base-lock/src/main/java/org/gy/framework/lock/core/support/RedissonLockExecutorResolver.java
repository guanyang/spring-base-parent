package org.gy.framework.lock.core.support;

import org.gy.framework.lock.core.DistributedLock;
import org.gy.framework.lock.core.LockExecutorResolver;
import org.gy.framework.lock.model.LockEntry;
import org.redisson.api.RedissonClient;
import org.springframework.util.Assert;

/**
 * @author guanyang
 */
public class RedissonLockExecutorResolver implements LockExecutorResolver {

    private final RedissonClient redissonClient;

    public RedissonLockExecutorResolver(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public DistributedLock resolve(LockEntry lock) {
        Assert.notNull(lock, () -> "LockEntry must not be null");
        Assert.notNull(redissonClient, () -> "RedissonClient must not be null");
        return new RedissonDistributedLock(redissonClient, lock.getLockKey(), lock.getExpireMillis());
    }
}
