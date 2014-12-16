package com.zoowii.jpa_utils.core;

import com.zoowii.jpa_utils.orm.Model;
import com.zoowii.jpa_utils.util.ListUtil;

import javax.persistence.*;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Session {
    private SessionFactory sessionFactory = null;
    protected EntityManager em = null;
    /**
     * 用来解决事务嵌套
     */
    protected final Queue<Object> txStack = new ConcurrentLinkedQueue<Object>();

    protected SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = SessionFactory.getDefaultSessionFactory();
        }
        return sessionFactory;
    }

    protected EntityManagerFactory getEntityManagerFactory() {
        return getSessionFactory().getEntityManagerFactory();
    }

    public static Session currentSession() {
        return SessionFactory.getDefaultSessionFactory().currentSession();
    }

    public static Session getSession(String persistenceUnit) {
        return SessionFactory.getSessionFactory(persistenceUnit).currentSession();
    }

    private Session() {
    }

    public Session(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public EntityManager getEntityManager() {
        if (em == null) {
            em = getEntityManagerFactory().createEntityManager();
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
        if (!isOpen()) {
            begin();
        }
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
        if (!isTransactionActive()) {
            return;
        }
        txStack.poll();
        if (getTransactionNestedLevel() > 0) {
            return;
        }
        getTransaction().rollback();
    }

    public boolean isClosed() {
        return !isOpen();
    }

    /**
     * 不需要手动自己调用,EntityManagerFactory的connection cleanup thread会自动清理
     */
    public void close() {
        if (isOpen()) {
            getEntityManager().close();
        }
    }

    /**
     * 关闭整个EntityManagerFactory不能再使用它来创建EntityManager了
     */
    public void shutdown() {
        getSessionFactory().close();
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

    public int executeNativeSql(String sql) {
        return getEntityManager().createNativeQuery(sql).executeUpdate();
    }

    public int executeQuerySql(String sql) {
        return getEntityManager().createQuery(sql).executeUpdate();
    }

    public List findListByQuery(Class cls, String queryString) {
        EntityManager em = getEntityManager();
        TypedQuery query = em.createQuery(queryString, cls);
        return query.getResultList();
    }

    public Object findFirstByQuery(Class cls, String queryString) {
        EntityManager em = getEntityManager();
        TypedQuery query = em.createQuery(queryString, cls);
        query.setMaxResults(1);
        return ListUtil.first(query.getResultList());
    }

    public Object findSingleBySql(Class cls, String sql) {
        Query query = getEntityManager().createQuery(sql, cls);
        query.setMaxResults(1);
        return query.getSingleResult();
    }

    /**
     * 直接执行原生SQL活得结果
     */
    public List findListByRawQuery(Class cls, String queryString) {
        EntityManager em = getEntityManager();
        javax.persistence.Query query = em.createNativeQuery(queryString, cls);
        return query.getResultList();
    }

    public List findListByRawQuery(String queryString) {
        EntityManager em = getEntityManager();
        javax.persistence.Query query = em.createNativeQuery(queryString);
        return query.getResultList();
    }

    public Object findFirstByRawQuery(Class cls, String queryString) {
        EntityManager em = getEntityManager();
        javax.persistence.Query query = em.createNativeQuery(queryString, cls);
        query.setMaxResults(1);
        return ListUtil.first(query.getResultList());
    }

    public Object findFirstByRawQuery(String queryString) {
        EntityManager em = getEntityManager();
        javax.persistence.Query query = em.createNativeQuery(queryString);
        query.setMaxResults(1);
        return ListUtil.first(query.getResultList());
    }

    public Object findSingleByNativeSql(Class cls, String sql) {
        Query query = getEntityManager().createNativeQuery(sql, cls);
        query.setMaxResults(1);
        return query.getSingleResult();
    }

    public Object findSingleByNativeSql(String sql) {
        Query query = getEntityManager().createNativeQuery(sql);
        query.setMaxResults(1);
        return query.getSingleResult();
    }
}
