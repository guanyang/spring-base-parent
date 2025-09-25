package io.github.guanyang.lock.core.support;

import lombok.SneakyThrows;
import io.github.guanyang.lock.core.DistributedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @author guanyang
 */
public class RedissonDistributedLock implements DistributedLock {

    private final RLock lock;
    /**
     * 过期时间，单位：毫秒
     */
    private final long expireMillis;

    public RedissonDistributedLock(RedissonClient client, String lockKey, long expireMillis) {
        this.expireMillis = expireMillis;
        this.lock = client.getLock(lockKey);
    }

    @Override
    @SneakyThrows
    public boolean tryLock() {
        return lock.tryLock(0, expireMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    @SneakyThrows
    public boolean tryLock(long waitTimeMillis, long sleepTimeMillis) {
        return lock.tryLock(waitTimeMillis, expireMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void lock(long sleepTimeMillis) {
        lock.lock(expireMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean unlock() {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            return true;
        }
        return false;
    }
}
