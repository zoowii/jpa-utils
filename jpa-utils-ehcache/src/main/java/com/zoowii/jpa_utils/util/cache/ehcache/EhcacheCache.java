package com.zoowii.jpa_utils.util.cache.ehcache;

import com.zoowii.jpa_utils.util.cache.ICache;
import net.sf.ehcache.Element;

import java.util.*;

/**
 * Created by zoowii on 16/2/20.
 */
public class EhcacheCache<K, V> implements ICache<K, V> {

    private net.sf.ehcache.Cache cache;

    public EhcacheCache(net.sf.ehcache.Cache cache) {
        this.cache = cache;
    }

    @Override
    public void put(K key, V value) {
        this.put(key, value, -1);
    }

    @Override
    public void put(K key, V value, int timeoutSeconds) {
        Element ele = new Element(key, value);
        if (timeoutSeconds > 0) {
            ele.setTimeToLive(timeoutSeconds);
        }
        cache.put(ele);
    }

    @Override
    public V get(K key) {
        Element ele = cache.get(key);
        if (ele != null) {
            return (V) ele.getObjectValue();
        } else {
            return null;
        }
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public void clean() {
        cache.removeAll();
    }

    @Override
    public int size() {
        return cache.getSize();
    }

    @Override
    public Set<K> keys() {
        List<Object> keys = cache.getKeys();
        Set<K> result = new HashSet<K>();
        result.addAll((List<K>) keys);
        return result;
    }

    @Override
    public Collection<V> values() {
        Set<K> cacheKeys = keys();
        List<V> result = new ArrayList<V>();
        for(K key : cacheKeys) {
            if(key == null) {
                continue;
            }
            V val = get(key);
            if(val != null) {
                result.add(val);
            }
        }
        return result;
    }
}
