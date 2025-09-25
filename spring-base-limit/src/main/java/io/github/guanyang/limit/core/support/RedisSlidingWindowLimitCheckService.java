package io.github.guanyang.limit.core.support;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import io.github.guanyang.limit.core.ILimitCheckService;
import io.github.guanyang.limit.enums.LimitTypeEnum;
import io.github.guanyang.limit.model.LimitCheckContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * 基于redis滑动窗口模式
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class RedisSlidingWindowLimitCheckService implements ILimitCheckService {

    private static final String DEFAULT_KEY_NAME = "sliding";

    private final StringRedisTemplate restTemplate;

    public RedisSlidingWindowLimitCheckService(StringRedisTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean check(LimitCheckContext context) {
        return internalExecute(context);
    }

    @Override
    public String type() {
        return LimitTypeEnum.SLIDING_WINDOW.getCode();
    }

    protected boolean internalExecute(LimitCheckContext context) {
        try {
            //基于脚本操作，保证原子性
            List<String> keys = getKeys(context.getKey(), DEFAULT_KEY_NAME);
            RedisScript<Long> script = RedisScript.of(LimitTypeEnum.scriptOf(type()), Long.class);
            Long result = restTemplate.execute(script, keys, String.valueOf(context.getTimeInMillis()), String.valueOf(context.getLimit()), IdUtil.simpleUUID(), String.valueOf(System.currentTimeMillis()));
            return !SUCCESS.equals(result);
        } catch (Exception e) {
            log.error("[ILimitCheckService]SLIDING_WINDOW error: {}", context, e);
            return false;
        }
    }
}
