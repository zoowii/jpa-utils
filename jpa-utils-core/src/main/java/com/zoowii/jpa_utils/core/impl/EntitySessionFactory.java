package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.AbstractSession;
import com.zoowii.jpa_utils.core.AbstractSessionFactory;
import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.util.StringUtil;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zoowii on 2014/10/18.
 */
public class EntitySessionFactory extends AbstractSessionFactory {
    private final EntityManagerFactory emf;
    private static final Map<String, WeakReference<EntitySessionFactory>> defaultEntitySessionFactories = new ConcurrentHashMap<String, WeakReference<EntitySessionFactory>>();

    public static void setDefaultEntitySessionFactory(String name, EntitySessionFactory sessionFactory) {
        if (StringUtil.notEmpty(name)) {
            defaultEntitySessionFactories.put(name, new WeakReference<EntitySessionFactory>(sessionFactory));
        }
    }

    private static EntitySessionFactory _getDefaultEntitySessionFactory(String name) {
        if (StringUtil.isEmpty(name)) {
            return null;
        }
        WeakReference<EntitySessionFactory> sessionFactoryRef = defaultEntitySessionFactories.get(name);
        return sessionFactoryRef != null ? sessionFactoryRef.get() : null;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static EntitySessionFactory getDefaultEntitySessionFactory() {
        return getEntitySessionFactory(getThreadDefaultPersistenceUnitName());
    }

    public static EntitySessionFactory getEntitySessionFactory(String persistenceUnit) {
        EntitySessionFactory sessionFactory = _getDefaultEntitySessionFactory(persistenceUnit);
        if (sessionFactory == null) {
            sessionFactory = new EntitySessionFactory(persistenceUnit);
        }
        return sessionFactory;
    }


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

    public EntitySessionFactory(String persistenceUnit) {
        emf = Persistence.createEntityManagerFactory(persistenceUnit);
        if (_getDefaultEntitySessionFactory(persistenceUnit) == null) {
            setDefaultEntitySessionFactory(persistenceUnit, this);
        }
    }

    public EntitySessionFactory() {
        this(getThreadDefaultPersistenceUnitName());
    }

    public EntitySessionFactory(EntityManagerFactory emf) {
        this.emf = emf;
        AbstractSession.setDefaultSessionFactoryIfEmpty(this);
    }

    @Override
    public Session createSession() {
        return new EntitySession(this);
    }

    @Override
    public void close() {
        emf.close();
    }
}
