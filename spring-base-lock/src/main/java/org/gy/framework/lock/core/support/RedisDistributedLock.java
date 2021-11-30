package org.gy.framework.lock.core.support;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.lock.core.AbstractDistributedLock;
import org.gy.framework.lock.core.DistributedLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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
            String script = "if redis.call('setNx',KEYS[1],ARGV[1]) == 1 then return redis.call('expire',KEYS[1],ARGV[2]) else return 0 end";

            RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);

            Object result = redisTemplate
                .execute(redisScript, Collections.singletonList(lockKey), requestId, String.valueOf(expireTime));

            if (SUCCESS.equals(result)) {
                return true;
            }
        } catch (Exception e) {
            log.error("[RedisDistributedLock]lock error:lockKey={},value={},expireTime={}.", lockKey, requestId,
                expireTime, e);
        }
        return false;
    }

    @Override
    public boolean innerUnLock(String lockKey, String requestId) {
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

            RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);

            Object result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), requestId);

            if (SUCCESS.equals(result)) {
                return true;
            }
        } catch (Exception e) {
            log.error("[RedisDistributedLock]release error:lockKey={},value={}.", lockKey, requestId, e);
        }
        return false;
    }


}
