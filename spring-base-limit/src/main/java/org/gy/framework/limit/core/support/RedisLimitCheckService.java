package org.gy.framework.limit.core.support;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.limit.core.ILimitCheckService;
import org.gy.framework.limit.enums.LimitTypeEnum;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * 频率限制检查默认实现（基于redis）
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class RedisLimitCheckService implements ILimitCheckService {

    private static final String DEFAULT_SCRIPT_STR = "local current; current = redis.call('incr',KEYS[1]); if tonumber(current) == 1 then redis.call('pexpire',KEYS[1],ARGV[1]); end; return current;";

    private static final RedisScript<Long> DEFAULT_SCRIPT = new DefaultRedisScript<>(DEFAULT_SCRIPT_STR, Long.class);

    private final StringRedisTemplate restTemplate;

    public RedisLimitCheckService(StringRedisTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String type() {
        return LimitTypeEnum.REDIS.getCode();
    }

    /**
     * 检查频率是否超过阈值，true是，false否
     */
    @Override
    public boolean check(LimitCheckContext context) {
        return innerIncr(context.getKey(), context.getTimeInMillis()) > context.getLimit();
    }

    private long innerIncr(String key, long expireMillis) {
        try {
            //基于脚本操作，保证原子性
            return restTemplate.execute(DEFAULT_SCRIPT, Collections.singletonList(key), String.valueOf(expireMillis));
        } catch (Exception e) {
            log.error("[ILimitCheckService]incr error:key={},expireMillis={}.", key, expireMillis, e);
        }
        return Long.MAX_VALUE;
    }
}
