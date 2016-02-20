package com.zoowii.jpa_utils.core;

/**
 * Created by zoowii on 14-12-23.
 */
public interface SessionFactory {
    Session getThreadScopeSession();

    Session currentSession();

    Session createSession();

    void close();

    void startCache();

    void endCache();

    void cacheBean(Object key, Class<?> beanCls, Object bean);

    <T> T getCachedBean(Object key, Class<? extends T> cls);

    void removeBeanCache(Object key, Class<?> beanCls);
}
