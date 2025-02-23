package org.gy.framework.lock;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gy.framework.lock.core.DistributedLock;
import org.gy.framework.lock.core.DistributedLockAction;
import org.gy.framework.lock.core.support.RedisDistributedLock;
import org.gy.framework.lock.model.LockResult;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class LockCommonTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void testLockRenewal() {
        Num num = new Num();
        StringRedisTemplate redisTemplate = initStringRedisTemplate();
        String redisKey = "GY:LOCK:TEST:" + System.currentTimeMillis();
        int expireTime = 3000;
        int sleepInterval = 5000;
        long expire = -1;
        DistributedLock lock = new RedisDistributedLock(redisTemplate, redisKey, expireTime, true);
        boolean lockFlag = false;
        try {
            lockFlag = lock.tryLock(-1, 50);
            if (!lockFlag) {
                throw new RuntimeException("lock failed.");
            }
            sleep(sleepInterval);
            num.incr();
        } finally {
            expire = redisTemplate.opsForValue().getOperations().getExpire(redisKey, TimeUnit.MILLISECONDS);
            if (lockFlag) {
                lock.unlock();
            }
        }
        log.info("expire value={}", expire);
        assertTrue(expire > 0);
    }

    @Test
    public void testOnceLock() {
        Num num = new Num();
        StringRedisTemplate redisTemplate = initStringRedisTemplate();
        String redisKey = "GY:LOCK:TEST:" + System.currentTimeMillis();
        int expireTime = 15;
        int expected = 1;
        DistributedLock lock = new RedisDistributedLock(redisTemplate, redisKey, expireTime);
        LockResult<Integer> result = DistributedLockAction.execute(lock, 30, () -> {
            return num.incr();
        });
        Integer actual = result.getData();
        log.info("expected value={}, actual value={}", expected, actual);
        assertEquals(expected, actual);
    }

    @Test
    public void testLock() {
        Num num = new Num();
        StringRedisTemplate redisTemplate = initStringRedisTemplate();
        String redisKey = "GY:LOCK:TEST:" + System.currentTimeMillis();
        int expireTime = 15;
        int expected = 50;
        List<Integer> data = execute(expected, () -> {
            DistributedLock lock = new RedisDistributedLock(redisTemplate, redisKey, expireTime);
            LockResult<Integer> result = DistributedLockAction.execute(lock, 30, () -> {
                return num.incr();
            });
            return result.getData();
        });
        Integer actual = data.stream().max(Integer::compareTo).orElse(0);
        log.info("expected value={}, actual value={}", expected, actual);
        assertEquals(expected, actual);
    }

    @Test
    public void testNonLock() {
        Num num = new Num();
        int expected = 50;
        List<Integer> data = execute(expected, () -> {
            return num.incr();
        });
        Integer actual = data.stream().max(Integer::compareTo).orElse(0);
        log.info("expected value={}, actual value={}", expected, actual);
        assertNotEquals(expected, actual);
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
        CompletableFuture<List<T>> result = allFuture.thenApply(
                s -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        List<T> data = result.join();
        log.info("exucute finish.");
        return data;
    }

    @Data
    public static class Num {

        private int num = 0;

        public int incr() {
            try {
                //模拟耗时
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            num++;
            log.info("current num={}", num);
            return num;
        }
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
