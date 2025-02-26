package org.gy.framework.limit;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.limit.annotation.LimitCheck;
import org.gy.framework.limit.core.ILimitCheckService;
import org.gy.framework.limit.core.support.LimitCheckContext;
import org.gy.framework.limit.core.support.RedisLimitCheckService;
import org.gy.framework.limit.core.support.RedisTokenBucketLimitCheckService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class LimitCommonTests {

    @Test
    void contextLoads() {
    }

    @Test
    void redisCheckServiceTest() {
        StringRedisTemplate redisTemplate = initStringRedisTemplate();
        ILimitCheckService checkService = new RedisLimitCheckService(redisTemplate);
        String redisKey = "GY:LIMIT:TEST:" + System.currentTimeMillis();
        int expected = 1;
        LimitCheckContext checkContext = LimitCheckContext.of(redisKey, 1000L, expected);
        List<Boolean> execute = execute(5, () -> {
            boolean checked = checkService.check(checkContext);
            sleep(50);
            return !checked;
        });
        long actual = execute.stream().filter(Boolean::booleanValue).count();
        assertEquals(expected, actual);
    }

    @Test
    void redisTokenBucketCheckServiceTest() {
        StringRedisTemplate redisTemplate = initStringRedisTemplate();
        ILimitCheckService checkService = new RedisTokenBucketLimitCheckService(redisTemplate);
        String redisKey = "GY:BUCKET_LIMIT:TEST:" + System.currentTimeMillis();
        int expected = 3;
        LimitCheckContext checkContext = LimitCheckContext.of(redisKey, 1000L, 1);
        checkContext.setCapacity(expected).setRequested(1);
        List<Boolean> execute = execute(5, () -> {
            boolean checked = checkService.check(checkContext);
            sleep(50);
            return !checked;
        });
        long actual = execute.stream().filter(Boolean::booleanValue).count();
        assertEquals(expected, actual);
    }

    private static <T> List<T> execute(int total, Supplier<T> supplier) {
        List<CompletableFuture<T>> futures = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier);
            futures.add(future);
        }
        log.info("begin allOf");
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        log.info("after allOf");
        CompletableFuture<List<T>> result = allFuture.thenApply(s -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        List<T> data = result.join();
        log.info("exucute finish.");
        return data;
    }

    @SneakyThrows
    private static void sleep(long timeout) {
        TimeUnit.MILLISECONDS.sleep(timeout);
    }

    private static StringRedisTemplate initStringRedisTemplate() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory("127.0.0.1", 6379);
        factory.setPassword("12345678");
        factory.afterPropertiesSet();
        return new StringRedisTemplate(factory);
    }

}
