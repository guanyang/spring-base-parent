package org.gy.framework.lock.core;


import org.gy.framework.lock.model.LockResult;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public class DistributedLockAction {


    /**
     * 功能描述：业务执行，包含加锁、释放锁(非阻塞锁，仅尝试一次获取锁)
     *
     * @param lock 分布式锁定义
     * @param runnable 执行体
     */
    public static <T> LockResult<T> execute(DistributedLock lock, DistributedLockCallback<T> runnable) {
        return execute(lock, 0, 0, runnable);
    }

    /**
     * 功能描述:业务执行，包含加锁、释放锁(阻塞锁，一直阻塞)
     *
     * @param lock 分布式锁定义
     * @param sleepTimeMillis 睡眠重试时间，单位：毫秒
     * @param runnable 执行体
     */
    public static <T> LockResult<T> execute(DistributedLock lock, long sleepTimeMillis,
        DistributedLockCallback<T> runnable) {
        return execute(lock, Long.MAX_VALUE, sleepTimeMillis, runnable);
    }

    /**
     * 功能描述:业务执行，包含加锁、释放锁(阻塞锁，自定义阻塞时间)
     *
     * @param lock 分布式锁定义
     * @param waitTimeMillis 等待超时时间，单位：毫秒
     * @param sleepTimeMillis 睡眠重试时间，单位：毫秒
     * @param runnable 执行体
     */
    public static <T> LockResult<T> execute(DistributedLock lock, long waitTimeMillis, long sleepTimeMillis,
        DistributedLockCallback<T> runnable) {
        boolean lockFlag = false;
        try {
            lockFlag = lock.tryLock(waitTimeMillis, sleepTimeMillis);
            if (!lockFlag) {
                return LockResult.wrapError();
            }
            T data = runnable.run();
            return LockResult.wrapSuccess(data);
        } finally {
            if (lockFlag) {
                lock.unlock();
            }
        }
    }

}
