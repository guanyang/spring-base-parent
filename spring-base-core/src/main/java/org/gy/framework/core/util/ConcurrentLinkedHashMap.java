package org.gy.framework.core.util;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ConcurrentLinkedHashMap<K, V> extends HashMap<K, V> implements Map<K, V>, Serializable {
    private static final long serialVersionUID = -70734360194688816L;

    private final LinkedHashMap<K, V> map;
    private final LockType lockType;

    //读写比例相对均衡
    private final ReentrantReadWriteLock rwl;
    //读操作远多于写操作（90%以上为读操作）
    private final StampedLock sl;

    public ConcurrentLinkedHashMap() {
        this(LockType.ReadWriteLock);
    }

    public ConcurrentLinkedHashMap(LockType lockType) {
        this(16, lockType);
    }


    public ConcurrentLinkedHashMap(int initialCapacity) {
        this(initialCapacity, LockType.ReadWriteLock);
    }

    public ConcurrentLinkedHashMap(int initialCapacity, LockType lockType) {
        this(initialCapacity, 0.75f, false, lockType);
    }

    public ConcurrentLinkedHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, false);
    }

    public ConcurrentLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        this(initialCapacity, loadFactor, accessOrder, LockType.ReadWriteLock);
    }

    public ConcurrentLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder, LockType lockType) {
        this.map = new LinkedHashMap<>(initialCapacity, loadFactor, accessOrder);
        this.lockType = lockType;
        if (Objects.equals(LockType.StampedLock, lockType)) {
            this.sl = new StampedLock();
            this.rwl = null;
        } else {
            this.rwl = new ReentrantReadWriteLock();
            this.sl = null;
        }
    }

    public ConcurrentLinkedHashMap(Map<? extends K, ? extends V> m) {
        this(m, LockType.ReadWriteLock);
    }

    public ConcurrentLinkedHashMap(Map<? extends K, ? extends V> m, LockType lockType) {
        this.map = new LinkedHashMap<>(m);
        this.lockType = lockType;
        if (Objects.equals(LockType.StampedLock, lockType)) {
            this.sl = new StampedLock();
            this.rwl = null;
        } else {
            this.rwl = new ReentrantReadWriteLock();
            this.sl = null;
        }
    }

    @Override
    public int size() {
        return doWithReadLock(Map::size);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return doWithReadLock(c -> c.containsKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return doWithReadLock(c -> c.containsValue(value));
    }

    @Override
    public V get(Object key) {
        return doWithReadLock(c -> c.get(key));
    }

    @Override
    public V put(K key, V value) {
        return doWithWriteLock(c -> c.put(key, value));
    }

    @Override
    public V remove(Object key) {
        return doWithWriteLock(c -> c.remove(key));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        doWithWriteLock(c -> {
            c.putAll(m);
            return null;
        });
    }

    @Override
    public void clear() {
        doWithWriteLock(c -> {
            c.clear();
            return null;
        });
    }

    @Override
    public Set<K> keySet() {
        return doWithReadLock(Map::keySet);
    }

    @Override
    public Collection<V> values() {
        return doWithReadLock(Map::values);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return doWithReadLock(Map::entrySet);
    }

    // 迭代操作（返回快照避免长期加锁）
    public Map<K, V> snapshot() {
        return doWithReadLock(m -> new LinkedHashMap<>(m));
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return doWithWriteLock(c -> c.putIfAbsent(key, value));
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        doWithWriteLock(c -> {
            c.replaceAll(function);
            return null;
        });
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        doWithReadLock(c -> {
            c.forEach(action);
            return null;
        });
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return doWithWriteLock(c -> c.merge(key, value, remappingFunction));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return doWithWriteLock(c -> c.compute(key, remappingFunction));
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return doWithWriteLock(c -> c.computeIfPresent(key, remappingFunction));
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return doWithWriteLock(c -> c.computeIfAbsent(key, mappingFunction));
    }

    @Override
    public V replace(K key, V value) {
        return doWithWriteLock(c -> c.replace(key, value));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return doWithWriteLock(c -> c.replace(key, oldValue, newValue));
    }

    @Override
    public boolean remove(Object key, Object value) {
        return doWithWriteLock(c -> c.remove(key, value));
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return doWithReadLock(c -> c.getOrDefault(key, defaultValue));
    }

    private <R> R doWithWriteLock(Action<K, V, R> action) {
        if (Objects.equals(LockType.StampedLock, lockType)) {
            return doWithWriteLock(sl, action);
        } else {
            return doWithWriteLock(rwl, action);
        }
    }

    private <R> R doWithReadLock(Action<K, V, R> action) {
        if (Objects.equals(LockType.StampedLock, lockType)) {
            return doWithReadLock(sl, action);
        } else {
            return doWithReadLock(rwl, action);
        }
    }

    private <R> R doWithWriteLock(ReentrantReadWriteLock rwl, Action<K, V, R> action) {
        rwl.writeLock().lock();
        try {
            return action.doWith(map);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    private <R> R doWithReadLock(ReentrantReadWriteLock rwl, Action<K, V, R> action) {
        rwl.readLock().lock();
        try {
            return action.doWith(map);
        } finally {
            rwl.readLock().unlock();
        }
    }

    private <R> R doWithWriteLock(StampedLock sl, Action<K, V, R> action) {
        long stamp = sl.writeLock();
        try {
            return action.doWith(map);
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    private <R> R doWithReadLock(StampedLock sl, Action<K, V, R> action) {
        long stamp = sl.tryOptimisticRead();
        R result = action.doWith(map);

        if (!sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                result = action.doWith(map);
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return result;
    }

    public enum LockType {
        StampedLock, ReadWriteLock
    }

    @FunctionalInterface
    interface Action<K, V, R> {
        R doWith(Map<K, V> map);
    }
}
