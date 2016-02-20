package com.zoowii.jpa_utils.util.cache;

import com.google.common.base.Function;
import com.zoowii.jpa_utils.util.ListUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zoowii on 16/2/20.
 */
public class MemoryCache<K, V> implements ICache<K, V> {
    private int maxEntriesCount = 10000;

    public int getMaxEntriesCount() {
        return maxEntriesCount;
    }

    public void setMaxEntriesCount(int maxEntriesCount) {
        this.maxEntriesCount = maxEntriesCount;
    }

    public boolean isCacheFull() {
        synchronized (cache) {
            return cache.size() >= maxEntriesCount;
        }
    }

    private final Map<K, Pair<V, Long>> cache = new ConcurrentHashMap<K, Pair<V, Long>>();

    @Override
    public void put(K key, V value) {
        if (isCacheFull()) {
            clean();
        }
        cache.put(key, Pair.of(value, -1L));
    }

    @Override
    public void put(K key, V value, int timeoutSeconds) {
        if (isCacheFull()) {
            clean();
        }
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, timeoutSeconds);
        cache.put(key, Pair.of(value, calendar.getTime().getTime()));
    }

    @Override
    public V get(K key) {
        Pair<V, Long> pair = cache.get(key);
        if(pair == null) {
            return null;
        }
        if(pair.getRight()<0) {
            return pair.getLeft();
        }
        Date date = new Date(pair.getRight());
        if(date.before(new Date())) {
            return null;
        } else {
            return pair.getLeft();
        }
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public void clean() {
        cache.clear();
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public Set<K> keys() {
        return cache.keySet();
    }

    @Override
    public Collection<V> values() {
        List<Pair<V, Long>> pairs = new ArrayList<Pair<V, Long>>();
        pairs.addAll(cache.values());
        return ListUtil.map(ListUtil.filter(pairs, new Function<Pair<V, Long>, Boolean>() {
            @Override
            public Boolean apply(Pair<V, Long> pair) {
                if (pair == null) {
                    return false;
                }
                if (pair.getRight() < 0) {
                    return true;
                }
                Date date = new Date(pair.getRight());
                if (date.before(new Date())) {
                    return false;
                } else {
                    return true;
                }
            }
        }), new Function<Pair<V, Long>, V>() {
            @Override
            public V apply(Pair<V, Long> pair) {
                return pair.getLeft();
            }
        });
    }
}
