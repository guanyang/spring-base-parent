package io.github.guanyang.lock.core;


import io.github.guanyang.lock.model.LockEntry;

/**
 * @author guanyang
 */
public interface LockExecutorResolver {

    DistributedLock resolve(LockEntry lock);
}
