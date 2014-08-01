package com.zoowii.hibernate_utils.core;

import javax.persistence.*;
import java.util.List;

public class Session {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("persistenceUnit");
    protected EntityManager em = null;
    private static final ThreadLocal<Session> sessionThreadLocal = new ThreadLocal<Session>();

    public static Session currentSession() {
        if (sessionThreadLocal.get() == null) {
            sessionThreadLocal.set(new Session());
        }
        return sessionThreadLocal.get();
    }

    private Session() {
    }

    public EntityManager getEntityManager() {
        if (em == null) {
            em = emf.createEntityManager();
        }
        return em;
    }

    public EntityTransaction getTransaction() {
        return getEntityManager().getTransaction();
    }

    public void begin() {
        getTransaction().begin();
    }

    public void commit() {
        getTransaction().commit();
    }

    public void rollback() {
        getTransaction().rollback();
    }

    public boolean isTransactionActive() {
        return getTransaction().isActive();
    }

    public void save(Object entity) {
        getEntityManager().persist(entity);
    }

    public void update(Object entity) {
        getEntityManager().persist(entity);
    }

    public void merge(Object entity) {
        getEntityManager().merge(entity);
    }

    public void refresh(Object entity) {
        getEntityManager().refresh(entity);
    }

    public Object find(Class cls, Object id) {
        return getEntityManager().find(cls, id);
    }

    public void delete(Object entity) {
        getEntityManager().remove(entity);
    }

    public void flush() {
        getEntityManager().flush();
    }

    public List findListByQuery(Class cls, String queryString) {
        EntityManager em = getEntityManager();
        TypedQuery query = em.createQuery(queryString, cls);
        return query.getResultList();
    }

    public Object findOneByQuery(Class cls, String queryString) {
        EntityManager em = getEntityManager();
        TypedQuery query = em.createQuery(queryString, cls);
        return query.getFirstResult();
    }

    /**
     * 直接执行原生SQL活得结果
     */
    public List findListByRawQuery(Class cls, String queryString) {
        EntityManager em = getEntityManager();
        javax.persistence.Query query = em.createNativeQuery(queryString, cls);
        return query.getResultList();
    }

    public Object findOneByRawQuery(Class cls, String queryString) {
        EntityManager em = getEntityManager();
        javax.persistence.Query query = em.createNativeQuery(queryString, cls);
        return query.getFirstResult();
    }
}
