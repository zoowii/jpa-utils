package com.zoowii.jpa_utils.core;

import com.zoowii.jpa_utils.core.impl.EntitySessionFactory;

import javax.persistence.EntityManagerFactory;

/**
 * Created by zoowii on 2015/1/18.
 */
public abstract class AbstractSessionFactory implements SessionFactory {
    public static SessionFactory getSessionFactory(EntityManagerFactory emf) {
        return new EntitySessionFactory(emf);
    }

    protected final ThreadLocal<Session> sessionThreadLocal = new ThreadLocal<Session>() {
        @Override
        public Session initialValue() {
            return createSession();
        }

        @Override
        public Session get() {
            Session session = super.get();
            if (session != null) {
                if (!session.isOpen()) {
                    this.remove();
                    session = initialValue();
                }
            }
            return session;
        }
    };

    @Override
    public Session getThreadScopeSession() {
        return sessionThreadLocal.get();
    }

    @Override
    public Session currentSession() {
        return getThreadScopeSession();
    }

    @Override
    public void startCache() {

    }

    @Override
    public void endCache() {

    }

    @Override
    public void cacheBean(Object key, Class<?> beanCls, Object bean) {

    }

    @Override
    public  <T> T getCachedBean(Object key, Class<? extends T> cls) {
        return null;
    }

    @Override
    public void removeBeanCache(Object key, Class<?> beanCls) {

    }
}
