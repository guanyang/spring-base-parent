package org.gy.framework.lock.core.support;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.lock.core.AbstractDistributedLock;
import org.gy.framework.lock.core.DistributedLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * 功能描述：redis分布式锁
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class RedisDistributedLock extends AbstractDistributedLock implements DistributedLock {

    private static final Long SUCCESS = 1L;

    private static final String LOCK_SCRIPT_STRING = "if redis.call('setNx',KEYS[1],ARGV[1]) == 1 then return redis.call('expire',KEYS[1],ARGV[2]) else return 0 end";
    private static final RedisScript<Long> LOCK_SCRIPT = RedisScript.of(LOCK_SCRIPT_STRING, Long.class);

    private static final String UNLOCK_SCRIPT_STRING = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    private static final RedisScript<Long> UNLOCK_SCRIPT = RedisScript.of(UNLOCK_SCRIPT_STRING, Long.class);

    /**
     * redis客户端
     */
    private StringRedisTemplate redisTemplate;

    public RedisDistributedLock(StringRedisTemplate redisTemplate, String lockKey, int expireTime) {
        super(lockKey, expireTime);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean innerLock(String lockKey, String requestId, int expireTime) {
        try {
            Long result = redisTemplate.execute(LOCK_SCRIPT, Collections.singletonList(lockKey), requestId,
                String.valueOf(expireTime));
            return SUCCESS.equals(result);
        } catch (Exception e) {
            log.error("[RedisDistributedLock]lock error:lockKey={},value={},expireTime={}.", lockKey, requestId,
                expireTime, e);
        }
        return false;
    }

    @Override
    public boolean innerUnLock(String lockKey, String requestId) {
        try {
            Long result = redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(lockKey), requestId);
            return SUCCESS.equals(result);
        } catch (Exception e) {
            log.error("[RedisDistributedLock]release error:lockKey={},value={}.", lockKey, requestId, e);
        }
        return false;
    }


}
