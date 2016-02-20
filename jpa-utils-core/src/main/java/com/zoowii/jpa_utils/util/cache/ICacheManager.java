package com.zoowii.jpa_utils.util.cache;

/**
 * Created by zoowii on 16/2/20.
 */
public interface ICacheManager {
    <K, V> ICache<K, V> getCache(String name);
}
