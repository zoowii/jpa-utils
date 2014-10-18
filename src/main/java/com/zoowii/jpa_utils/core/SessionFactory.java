package com.zoowii.jpa_utils.core;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by 维 on 2014/10/18.
 */
public final class SessionFactory {
    private final EntityManagerFactory emf;
    private static SessionFactory defaultSessionFactory = null;

    public static void setDefaultSessionFactory(SessionFactory sessionFactory) {
        defaultSessionFactory = sessionFactory;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static SessionFactory getDefaultSessionFactory() {
        if (defaultSessionFactory == null) {
            defaultSessionFactory = new SessionFactory();
        }
        return defaultSessionFactory;
    }

    private final ThreadLocal<Session> sessionThreadLocal = new ThreadLocal<Session>() {
        @Override
        public Session initialValue() {
            return createSession();
        }
    };

    public Session currentSession() {
        return sessionThreadLocal.get();
    }

    public SessionFactory(String persistenceUnit) {
        emf = Persistence.createEntityManagerFactory(persistenceUnit);
        if ("persistenceUnit".equals(persistenceUnit) && defaultSessionFactory == null) {
            defaultSessionFactory = this;
        }
    }

    public SessionFactory() {
        this("persistenceUnit");
    }

    public Session createSession() {
        return new Session(emf);
    }

    public void close() {
        emf.close();
        if (defaultSessionFactory == null) {
            return;
        }
        synchronized (defaultSessionFactory) {
            if (defaultSessionFactory == this) {
                defaultSessionFactory = null;
            }
        }
    }

}
