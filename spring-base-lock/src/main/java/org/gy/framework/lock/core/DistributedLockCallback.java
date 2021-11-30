package org.gy.framework.lock.core;

/**
 * 功能描述：
 *
 * @author gy
 * @version 1.0.0
 */
public interface DistributedLockCallback<T> {

    T run();
}
