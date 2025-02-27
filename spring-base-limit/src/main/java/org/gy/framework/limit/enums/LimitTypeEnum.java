package org.gy.framework.limit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.core.support.IStdEnum;
import org.gy.framework.limit.core.ILimitCheckService;
import org.gy.framework.limit.core.support.RedisLimitCheckService;
import org.gy.framework.limit.core.support.RedisSlidingWindowLimitCheckService;
import org.gy.framework.limit.core.support.RedisTokenBucketLimitCheckService;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 频率限制类型定义
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
@Getter
@AllArgsConstructor
public enum LimitTypeEnum implements IStdEnum<String> {

    TIME_WINDOW("TIME_WINDOW", "redis时间窗口模式", RedisLimitCheckService.class, "scripts/rateLimit-timeWindow.lua"),

    TOKEN_BUCKET("TOKEN_BUCKET", "redis令牌桶模式", RedisTokenBucketLimitCheckService.class, "scripts/rateLimit-tokenBucket.lua"),

    SLIDING_WINDOW("SLIDING_WINDOW", "redis滑动窗口模式", RedisSlidingWindowLimitCheckService.class, "scripts/rateLimit-slidingWindow.lua");

    private final String code;

    private final String desc;

    private final Class<? extends ILimitCheckService> checkClass;

    private final String scriptFileName;

    public static final Map<String, String> REDIS_SCRIPTS = new ConcurrentHashMap<>();

    static {
        Arrays.stream(LimitTypeEnum.values()).filter(item -> StringUtils.hasText(item.getScriptFileName())).forEach(item -> {
            REDIS_SCRIPTS.put(item.getCode(), getRateLimiterScript(item.getScriptFileName()));
        });
    }

    public static LimitTypeEnum codeOf(String code) {
        LimitTypeEnum item = LimitTypeEnum.codeOf(code, null);
        return Objects.requireNonNull(item, () -> "unknown LimitTypeEnum error:" + code);
    }

    public static LimitTypeEnum codeOf(String code, LimitTypeEnum defaultEnum) {
        return IStdEnum.codeOf(LimitTypeEnum.class, code, defaultEnum);
    }

    public static String scriptOf(LimitTypeEnum item) {
        return Optional.ofNullable(item).map(i -> REDIS_SCRIPTS.get(i.getCode())).orElse(null);
    }

    public static String scriptOf(String code) {
        return Optional.ofNullable(code).map(REDIS_SCRIPTS::get).orElse(null);
    }

    private static String getRateLimiterScript(String scriptFileName) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(scriptFileName)) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("lua Initialization failure: " + scriptFileName, e);
        }
    }

}
