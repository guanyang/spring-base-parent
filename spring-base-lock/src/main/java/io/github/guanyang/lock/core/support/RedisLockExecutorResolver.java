package io.github.guanyang.lock.core.support;

import io.github.guanyang.lock.core.DistributedLock;
import io.github.guanyang.lock.core.LockExecutorResolver;
import io.github.guanyang.lock.model.LockEntry;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

/**
 * @author guanyang
 */
public class RedisLockExecutorResolver implements LockExecutorResolver {
    /**
     * redis客户端
     */
    private final StringRedisTemplate stringRedisTemplate;

    public RedisLockExecutorResolver(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public DistributedLock resolve(LockEntry lock) {
        Assert.notNull(lock, () -> "LockEntry must not be null");
        Assert.notNull(stringRedisTemplate, () -> "StringRedisTemplate must not be null");
        return new RedisDistributedLock(stringRedisTemplate, lock.getLockKey(), lock.getExpireMillis(), lock.isRenewal());
    }
}
