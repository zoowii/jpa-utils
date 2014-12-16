package com.zoowii.jpa_utils.core;

import com.zoowii.jpa_utils.util.StringUtil;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 维 on 2014/10/18.
 */
public final class SessionFactory {
    private final EntityManagerFactory emf;
    private static final Map<String, WeakReference<SessionFactory>> defaultSessionFactories = new ConcurrentHashMap<String, WeakReference<SessionFactory>>();

    public static void setDefaultSessionFactory(String name, SessionFactory sessionFactory) {
        if (StringUtil.notEmpty(name)) {
            defaultSessionFactories.put(name, new WeakReference<SessionFactory>(sessionFactory));
        }
    }

    private static SessionFactory _getDefaultSessionFactory(String name) {
        if (StringUtil.isEmpty(name)) {
            return null;
        }
        WeakReference<SessionFactory> sessionFactoryRef = defaultSessionFactories.get(name);
        return sessionFactoryRef != null ? sessionFactoryRef.get() : null;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static SessionFactory getDefaultSessionFactory() {
        return getSessionFactory(getThreadDefaultPersistenceUnitName());
    }

    public static SessionFactory getSessionFactory(String persistenceUnit) {
        SessionFactory sessionFactory = _getDefaultSessionFactory(persistenceUnit);
        if (sessionFactory == null) {
            sessionFactory = new SessionFactory(persistenceUnit);
        }
        return sessionFactory;
    }

    private final ThreadLocal<Session> sessionThreadLocal = new ThreadLocal<Session>() {
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
    private static final ThreadLocal<String> threadDefaultPersistenceUnitName = new ThreadLocal<String>() {
        @Override
        public String initialValue() {
            return "persistenceUnit";
        }
    };

    public static void setThreadDefaultPersistenceUnitName(String name) {
        if (name != null && name.trim().length() > 0) {
            threadDefaultPersistenceUnitName.set(name);
        }
    }

    public static String getThreadDefaultPersistenceUnitName() {
        return threadDefaultPersistenceUnitName.get();
    }

    public Session currentSession() {
        return sessionThreadLocal.get();
    }

    public SessionFactory(String persistenceUnit) {
        emf = Persistence.createEntityManagerFactory(persistenceUnit);
        if (_getDefaultSessionFactory(persistenceUnit) == null) {
            setDefaultSessionFactory(persistenceUnit, this);
        }
    }

    public SessionFactory() {
        this(getThreadDefaultPersistenceUnitName());
    }

    public Session createSession() {
        return new Session(this);
    }

    public void close() {
        emf.close();
    }
}
