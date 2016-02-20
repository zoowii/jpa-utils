package com.zoowii.jpa_utils.util.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zoowii on 16/2/20.
 */
public class MemoryCacheManager implements ICacheManager {
    private final Map<String, ICache> caches = new ConcurrentHashMap<String, ICache>();

    @Override
    public <K, V> ICache<K, V> getCache(String name) {
        synchronized (caches) {
            if (caches.containsKey(name) && caches.get(name) != null) {
                return caches.get(name);
            }
            ICache<K, V> cache = new MemoryCache<K, V>();
            caches.put(name, cache);
            return cache;
        }
    }
}
