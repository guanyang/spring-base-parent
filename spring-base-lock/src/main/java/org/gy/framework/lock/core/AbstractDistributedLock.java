package org.gy.framework.lock.core;


import static org.gy.framework.lock.utils.IdUtils.defaultRequestId;

import lombok.extern.slf4j.Slf4j;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public abstract class AbstractDistributedLock implements DistributedLock {

    public static final long SLEEP_TIME_MILLIS = 50;

    public static final int EXPIRE_TIME_SECOND = 5;
    /**
     * 锁key
     */
    private String lockKey;
    /**
     * 锁的值，请求ID
     */
    private String requestId;
    /**
     * 过期时间，单位：秒
     */
    private int expireTime;
    /**
     * 加锁是否成功，true是，false否
     */
    private volatile boolean locked = false;

    public AbstractDistributedLock(String lockKey) {
        this(lockKey, EXPIRE_TIME_SECOND);
    }

    public AbstractDistributedLock(String lockKey, int expireTime) {
        this(lockKey, defaultRequestId(), expireTime);
    }

    public AbstractDistributedLock(String lockKey, String requestId, int expireTime) {
        this.lockKey = lockKey;
        this.requestId = requestId;
        this.expireTime = checkExpireTime(expireTime);
    }


    private static int checkExpireTime(int expireTime) {
        return expireTime > 0 ? expireTime : EXPIRE_TIME_SECOND;
    }

    private static long checkSleepTime(long sleepTimeMillis) {
        return sleepTimeMillis > 0 ? sleepTimeMillis : SLEEP_TIME_MILLIS;
    }

    @Override
    public boolean tryLock() {
        return tryLock(0, 0);
    }

    @Override
    public boolean tryLock(long waitTimeMillis, long sleepTimeMillis) {
        try {
            long startTime = System.currentTimeMillis();
            if (waitTimeMillis == 0) {
                //此情况，不需要等待重试
                locked = innerLock(lockKey, requestId, expireTime);
                return locked;
            }
            sleepTimeMillis = checkSleepTime(sleepTimeMillis);
            while (checkCondition(startTime, waitTimeMillis)) {
                locked = innerLock(lockKey, requestId, expireTime);
                if (locked) {
                    return true;
                }
                // 针对有超时的场景，如果此时耗费时间加上sleepTime已经超时，直接返回失败即可，不需要sleep，可提升效率
                if (waitTimeMillis > 0) {
                    long time = (System.currentTimeMillis() - startTime) + sleepTimeMillis - waitTimeMillis;
                    if (time > 0) {
                        return false;
                    }
                }
                wrapBlockTime(sleepTimeMillis);
            }
        } catch (Exception e) {
            log.error("[DistributedLock]加锁异常:waitTimeMillis={},sleepTimeMillis={}.",
                waitTimeMillis, sleepTimeMillis, e);
        }
        return false;
    }

    @Override
    public void lock(long sleepTimeMillis) {
        tryLock(-1, sleepTimeMillis);
    }

    @Override
    public boolean unlock() {
        if (locked) {
            boolean unLock = innerUnLock(lockKey, requestId);
            if (unLock) {
                locked = false;
            }
            return unLock;
        }
        return false;
    }

    private boolean checkCondition(long startTime, long waitTimeMillis) {
        if (waitTimeMillis < 0) {
            return true;
        }
        return (System.currentTimeMillis() - startTime) <= waitTimeMillis;
    }

    private void wrapBlockTime(long sleepTimeMillis) {
        try {
            Thread.sleep(sleepTimeMillis);
        } catch (InterruptedException e) {
            log.error("[DistributedLock]线程被中断" + Thread.currentThread().getId(), e);
        }
    }


    /**
     * 功能描述：获取锁
     *
     * @param lockKey 锁key
     * @param requestId 锁的值，请求ID
     * @param expireTime 过期时间，单位：秒
     * @return 获取锁是否成功，true是，false否
     * @author gy
     */
    public abstract boolean innerLock(String lockKey, String requestId, int expireTime);

    /**
     * 功能描述：释放锁
     *
     * @param lockKey 锁key
     * @param requestId 锁的值，请求ID
     * @return 释放锁是否成功，true是，false否
     * @author gy
     */
    public abstract boolean innerUnLock(String lockKey, String requestId);

}
