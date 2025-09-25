package io.github.guanyang.lock.core.support;

import io.github.guanyang.lock.core.DistributedLock;
import io.github.guanyang.lock.core.LockExecutorResolver;
import io.github.guanyang.lock.model.LockEntry;
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
