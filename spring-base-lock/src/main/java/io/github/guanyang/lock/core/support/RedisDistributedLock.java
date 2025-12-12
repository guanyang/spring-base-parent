package io.github.guanyang.lock.core.support;

import io.github.guanyang.lock.core.AbstractDistributedLock;
import io.github.guanyang.lock.core.DistributedLock;
import lombok.extern.slf4j.Slf4j;
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
    private static final int DEFAULT_SCHEDULE_THREAD_COUNT = Math.min(Runtime.getRuntime().availableProcessors(), 4);

    // lua脚本加锁
    private static final String LOCK_SCRIPT_STRING = "if redis.call('setNx',KEYS[1],ARGV[1]) == 1 then return redis.call('pexpire',KEYS[1],ARGV[2]) else return 0 end";
    private static final RedisScript<Long> LOCK_SCRIPT = RedisScript.of(LOCK_SCRIPT_STRING, Long.class);
    // lua脚本解锁
    private static final String UNLOCK_SCRIPT_STRING = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    private static final RedisScript<Long> UNLOCK_SCRIPT = RedisScript.of(UNLOCK_SCRIPT_STRING, Long.class);
    // lua脚本续期
    private static final String RENEW_SCRIPT_STRING = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('pexpire', KEYS[1], ARGV[2]) else return 0 end";
    private static final RedisScript<Long> RENEW_SCRIPT = new DefaultRedisScript<>(RENEW_SCRIPT_STRING, Long.class);

    //续期任务调度，大量数据时慎用，会占用过多内存
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> RENEWAL_TASKS = new ConcurrentHashMap<>();

    /**
     * redis客户端
     */
    private final StringRedisTemplate redisTemplate;

    //考虑到续期各种异常因素影响，默认不开启，大量数据时慎用，会占用过多内存
    private final boolean renewal;

    public RedisDistributedLock(StringRedisTemplate redisTemplate, String lockKey, long expireMillis) {
        this(redisTemplate, lockKey, expireMillis, false);
    }

    public RedisDistributedLock(StringRedisTemplate redisTemplate, String lockKey, long expireMillis, boolean renewal) {
        super(lockKey, expireMillis);
        this.redisTemplate = redisTemplate;
        this.renewal = renewal;
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
        if (!renewal){
            log.debug("[RedisDistributedLock]renewal disabled:lockKey={},value={},expireMillis={}.", lockKey, requestId, expireMillis);
            return;
        }
        // 续期间隔设置为过期时间的 1/3
        long renewInterval = expireMillis / 3;
        ScheduledFuture<?> future = getScheduler().scheduleAtFixedRate(() -> {
            boolean result = innerRenewal(lockKey, requestId, expireMillis);
            log.debug("[RedisDistributedLock]renewal result:lockKey={},requestId={},expireMillis={},renewInterval={},result={}.", lockKey, requestId, expireMillis, renewInterval, result);
            if (!result) {
                cancelRenewal(lockKey);
            }
        }, renewInterval, renewInterval, TimeUnit.MILLISECONDS);

        RENEWAL_TASKS.put(lockKey, future);
    }


    protected void cancelRenewal(String lockKey) {
        if (!renewal){
            log.debug("[RedisDistributedLock]renewal disabled:lockKey={}.", lockKey);
            return;
        }
        ScheduledFuture<?> future = RENEWAL_TASKS.remove(lockKey);
        if (future != null) {
            future.cancel(true);
        }
    }

    private static ScheduledExecutorService getScheduler() {
        return SchedulerHolder.INSTANCE;
    }

    private static class SchedulerHolder {
        private static final ScheduledExecutorService INSTANCE = Executors.newScheduledThreadPool(DEFAULT_SCHEDULE_THREAD_COUNT);
    }


}
