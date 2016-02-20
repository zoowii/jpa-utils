package com.zoowii.jpa_utils.util.cache;

/**
 * Created by zoowii on 16/2/20.
 */
public class CacheManagerBuilder {
    public static ICacheManager createCacheManagerBuilder(String clsFullName) throws Exception {
        Class<?> cls = Class.forName(clsFullName);
        if(cls == null || !ICacheManager.class.isAssignableFrom(cls)) {
            return null;
        }
        return (ICacheManager) cls.newInstance();
    }
}
