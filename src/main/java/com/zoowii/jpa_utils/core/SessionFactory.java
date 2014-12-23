package com.zoowii.jpa_utils.core;

import com.zoowii.jpa_utils.core.impl.EntitySessionFactory;

import javax.persistence.EntityManagerFactory;

/**
 * Created by zoowii on 14-12-23.
 */
public abstract class SessionFactory {
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

    protected Session getThreadScopeSession() {
        return sessionThreadLocal.get();
    }

    public Session currentSession() {
        return getThreadScopeSession();
    }

    public abstract Session createSession();

    public abstract void close();
}
