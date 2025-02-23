package org.gy.framework.lock.core.support;

import lombok.extern.slf4j.Slf4j;
import org.gy.framework.lock.core.AbstractDistributedLock;
import org.gy.framework.lock.core.DistributedLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.concurrent.*;

/**
 * 功能描述：redis分布式锁
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class RedisDistributedLock extends AbstractDistributedLock implements DistributedLock {

    private static final Long SUCCESS = 1L;
    private static final int DEFAULT_SCHEDULE_THREAD_COUNT = Math.max(Runtime.getRuntime().availableProcessors(), 4);

    // lua脚本加锁
    private static final String LOCK_SCRIPT_STRING = "if redis.call('setNx',KEYS[1],ARGV[1]) == 1 then return redis.call('pexpire',KEYS[1],ARGV[2]) else return 0 end";
    private static final RedisScript<Long> LOCK_SCRIPT = RedisScript.of(LOCK_SCRIPT_STRING, Long.class);
    // lua脚本解锁
    private static final String UNLOCK_SCRIPT_STRING = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    private static final RedisScript<Long> UNLOCK_SCRIPT = RedisScript.of(UNLOCK_SCRIPT_STRING, Long.class);
    // lua脚本续期
    private static final String RENEW_SCRIPT_STRING = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('pexpire', KEYS[1], ARGV[2]) else return 0 end";
    private static final RedisScript<Long> RENEW_SCRIPT = new DefaultRedisScript<>(RENEW_SCRIPT_STRING, Long.class);

    //续期任务调度
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(DEFAULT_SCHEDULE_THREAD_COUNT);
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> RENEWAL_TASKS = new ConcurrentHashMap<>();

    /**
     * redis客户端
     */
    private final StringRedisTemplate redisTemplate;

    public RedisDistributedLock(StringRedisTemplate redisTemplate, String lockKey, long expireMillis) {
        super(lockKey, expireMillis);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean innerLock(String lockKey, String requestId, long expireMillis) {
        try {
            Long value = redisTemplate.execute(LOCK_SCRIPT, Collections.singletonList(lockKey), requestId, String.valueOf(expireMillis));
            boolean result = SUCCESS.equals(value);
            if (result) {
                // 加锁成功，开启续期任务
                scheduleRenewal(lockKey, requestId, expireMillis);
            }
            return result;
        } catch (Exception e) {
            log.error("[RedisDistributedLock]lock error:lockKey={},value={},expireMillis={}.", lockKey, requestId, expireMillis, e);
        }
        return false;
    }

    @Override
    public boolean innerUnLock(String lockKey, String requestId) {
        try {
            Long value = redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(lockKey), requestId);
            boolean result = SUCCESS.equals(value);
            if (result) {
                // 释放锁成功，取消续期任务
                cancelRenewal(lockKey);
            }
            return result;
        } catch (Exception e) {
            log.error("[RedisDistributedLock]release error:lockKey={},value={}.", lockKey, requestId, e);
        }
        return false;
    }

    protected boolean innerRenewal(String lockKey, String requestId, long expireMillis) {
        try {
            Long result = redisTemplate.execute(RENEW_SCRIPT, Collections.singletonList(lockKey), requestId, String.valueOf(expireMillis));
            return SUCCESS.equals(result);
        } catch (Exception e) {
            log.error("[RedisDistributedLock]renewal error:lockKey={},value={},expireMillis={}.", lockKey, requestId, expireMillis, e);
        }
        return false;
    }

    protected void scheduleRenewal(String lockKey, String requestId, long expireMillis) {
        // 续期间隔设置为过期时间的 1/3
        long renewInterval = expireMillis / 3;
        ScheduledFuture<?> future = SCHEDULER.scheduleAtFixedRate(() -> {
            boolean result = innerRenewal(lockKey, requestId, expireMillis);
            log.debug("[RedisDistributedLock]renewal result:lockKey={},requestId={},expireMillis={},renewInterval={},result={}.", lockKey, requestId, expireMillis, renewInterval, result);
            if (!result) {
                cancelRenewal(lockKey);
            }
        }, renewInterval, renewInterval, TimeUnit.MILLISECONDS);

        RENEWAL_TASKS.put(lockKey, future);
    }


    protected void cancelRenewal(String lockKey) {
        ScheduledFuture<?> future = RENEWAL_TASKS.remove(lockKey);
        if (future != null) {
            future.cancel(true);
        }
    }


}
