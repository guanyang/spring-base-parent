package org.gy.framework.lock.core;


import org.gy.framework.lock.model.LockEntry;

/**
 * @author guanyang
 */
public interface LockExecutorResolver {

    DistributedLock resolve(LockEntry lock);
}
