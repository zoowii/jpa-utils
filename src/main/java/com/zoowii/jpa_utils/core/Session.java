package com.zoowii.jpa_utils.core;

import javax.persistence.*;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Session {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("persistenceUnit");
    protected EntityManager em = null;
    /**
     * 用来解决事务嵌套
     */
    protected final Queue<Object> txStack = new ConcurrentLinkedQueue<Object>();
    private static final ThreadLocal<Session> sessionThreadLocal = new ThreadLocal<Session>() {
        @Override
        public Session initialValue() {
            return new Session();
        }
    };

    public static Session currentSession() {
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
        txStack.add(1);
        if (getTransactionNestedLevel() > 1) {
            return;
        }
        getTransaction().begin();
    }

    public void commit() {
        txStack.poll();
        if (getTransactionNestedLevel() > 0) {
            return;
        }
        getTransaction().commit();
    }

    public boolean isOpen() {
        return getEntityManager().isOpen();
    }

    public void rollback() {
        try {
            getTransaction().rollback();
        } finally {
            txStack.poll();
        }
    }

    /**
     * 获取事务嵌套层数
     *
     * @return
     */
    public int getTransactionNestedLevel() {
        return txStack.size();
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
