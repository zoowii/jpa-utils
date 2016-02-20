package com.zoowii.jpa_utils.util.cache.ehcache;

import com.zoowii.jpa_utils.util.cache.ICache;
import com.zoowii.jpa_utils.util.cache.ICacheManager;
import net.sf.ehcache.CacheManager;

/**
 * Created by zoowii on 16/2/20.
 */
public class EhcacheCacheManager implements ICacheManager {
    private CacheManager cacheManager;

    public EhcacheCacheManager() {
        this("META-INF/ehcache/ehcache.xml");
    }

    public EhcacheCacheManager(String configPath) {
        this.cacheManager = CacheManager.newInstance(this.getClass().getClassLoader().getResourceAsStream(configPath));
    }

    @Override
    public <K, V> ICache<K, V> getCache(String name) {
        return new EhcacheCache<K, V>(this.cacheManager.getCache(name));
    }
}
