package sgw.core.util;

import org.apache.http.annotation.ThreadSafe;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * For situations that read ops vastly outnumber write ops.
 * Copy the whole map whenever mutation happens.
 * Concurrent read op won't block thread, but concurrent writes will.
 * @param <K>
 * @param <V>
 */
@ThreadSafe
public class CopyOnWriteHashMap<K, V> implements Map<K, V> {

    private volatile HashMap<K, V> map;

    private final Lock lock = new ReentrantLock();

    public CopyOnWriteHashMap() {
        map = new HashMap<>();
    }

    public CopyOnWriteHashMap(HashMap<K, V> hashmap) {
        map = hashmap;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        lock.lock();
        try {
            HashMap<K, V> newMap = new HashMap<>(map);
            V val = newMap.remove(key);
            map = newMap;
            return val;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        if (map == null) {
            return;
        }
        lock.lock();
        try {
            HashMap<K, V> newMap = new HashMap<>(this.map);
            newMap.putAll(map);
            this.map = newMap;
        } finally {
            lock.unlock();
        }
    }

    public void clearAndPutAll(Map<? extends K, ? extends  V> map) {
        if (map == null) {
            return;
        }
        lock.lock();
        try {
            this.map = new HashMap<>(map);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        lock.lock();
        try {
            HashMap<K, V> newMap = new HashMap<>(map);
            V val = newMap.remove(key);
            map = newMap;
            return val;
        } finally {
            lock.unlock();
        }
    }

    public void removeAll(Collection<K> keys) {
        lock.lock();
        try {
            HashMap<K, V> newMap = new HashMap<>(map);
            for (K key: keys) {
                newMap.remove(key);
            }
            this.map = newMap;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            HashMap<K, V> newMap = new HashMap<>();
            newMap.clear();
            map = newMap;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableCollection(map.values());
    }


}
