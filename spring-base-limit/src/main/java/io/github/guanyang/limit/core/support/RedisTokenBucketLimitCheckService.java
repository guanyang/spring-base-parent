package io.github.guanyang.limit.core.support;

import lombok.extern.slf4j.Slf4j;
import io.github.guanyang.core.util.CollectionUtils;
import io.github.guanyang.limit.core.ILimitCheckService;
import io.github.guanyang.limit.enums.LimitTypeEnum;
import io.github.guanyang.limit.model.LimitCheckContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Instant;
import java.util.List;

/**
 * 基于redis令牌桶模式
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class RedisTokenBucketLimitCheckService implements ILimitCheckService {

    private static final String TOKENS_KEY_NAME = "tokens";
    private static final String TIMESTAMP_KEY_NAME = "timestamp";

    private final StringRedisTemplate restTemplate;

    public RedisTokenBucketLimitCheckService(StringRedisTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean check(LimitCheckContext context) {
        return internalExecute(context);
    }

    @Override
    public String type() {
        return LimitTypeEnum.TOKEN_BUCKET.getCode();
    }

    protected boolean internalExecute(LimitCheckContext context) {
        try {
            //基于脚本操作，保证原子性
            List<String> keys = getKeys(context.getKey(), TOKENS_KEY_NAME, TIMESTAMP_KEY_NAME);
            RedisScript<?> script = RedisScript.of(LimitTypeEnum.scriptOf(type()), List.class);
            List<Long> result = (List<Long>) restTemplate.execute(script, keys, String.valueOf(context.getLimit()), String.valueOf(context.getCapacity()), String.valueOf(context.getRequested()), String.valueOf(Instant.now().getEpochSecond()));
            return CollectionUtils.isNotEmpty(result) && !SUCCESS.equals(result.get(0));
        } catch (Exception e) {
            log.error("[ILimitCheckService]TOKEN_BUCKET error: {}", context, e);
            return false;
        }
    }
}
