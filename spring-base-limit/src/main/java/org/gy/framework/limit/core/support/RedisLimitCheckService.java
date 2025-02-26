package org.gy.framework.limit.core.support;

import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.util.CollectionUtils;
import org.gy.framework.limit.core.ILimitCheckService;
import org.gy.framework.limit.enums.LimitTypeEnum;
import org.gy.framework.limit.model.LimitCheckContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * 基于redis时间窗口模式
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class RedisLimitCheckService implements ILimitCheckService {

    private static final String DEFAULT_KEY_NAME = "default";

    private final StringRedisTemplate restTemplate;

    public RedisLimitCheckService(StringRedisTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String type() {
        return LimitTypeEnum.TIME_WINDOW.getCode();
    }

    /**
     * 检查频率是否超过阈值，true是，false否
     */
    @Override
    public boolean check(LimitCheckContext context) {
        return internalExecute(context);
    }

    protected boolean internalExecute(LimitCheckContext context) {
        try {
            //基于脚本操作，保证原子性
            List<String> keys = getKeys(context.getKey(), DEFAULT_KEY_NAME);
            RedisScript<?> script = RedisScript.of(LimitTypeEnum.scriptOf(type()), List.class);
            List<Long> result = (List<Long>) restTemplate.execute(script, keys, String.valueOf(context.getLimit()), String.valueOf(context.getTimeInMillis()));
            return CollectionUtils.isNotEmpty(result) && !SUCCESS.equals(result.get(0));
        } catch (Exception e) {
            log.error("[ILimitCheckService]redis error: {}", context, e);
            return false;
        }
    }
}
