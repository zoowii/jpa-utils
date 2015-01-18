package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.*;
import com.zoowii.jpa_utils.query.Expr;
import com.zoowii.jpa_utils.query.ParameterBindings;
import com.zoowii.jpa_utils.query.QueryInfo;
import com.zoowii.jpa_utils.util.ListUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;

public class EntitySession extends AbstractSession {
    private EntitySessionFactory sessionFactory = null;
    protected EntityManager em = null;

    protected EntitySessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = EntitySessionFactory.getDefaultEntitySessionFactory();
        }
        return sessionFactory;
    }

    protected EntityManagerFactory getEntityManagerFactory() {
        return getSessionFactory().getEntityManagerFactory();
    }

    public static Session currentSession() {
        return EntitySessionFactory.getDefaultEntitySessionFactory().currentSession();
    }

    public static Session getSession(String persistenceUnit) {
        return EntitySessionFactory.getEntitySessionFactory(persistenceUnit).currentSession();
    }

    protected EntitySession() {
    }

    public EntitySession(EntityManager entityManager) {
        this.em = entityManager;
    }

    public EntitySession(EntitySessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public EntityManager getEntityManager() {
        if (em == null) {
            em = getEntityManagerFactory().createEntityManager();
        }
        return em;
    }

    @Override
    public Transaction getTransaction() {
        return new EntityTransaction(getEntityManager().getTransaction());
    }


    @Override
    public boolean isOpen() {
        return getEntityManager().isOpen();
    }

    /**
     * 不需要手动自己调用,EntityManagerFactory的connection cleanup thread会自动清理
     */
    @Override
    public void close() {
        if (isOpen()) {
            getEntityManager().close();
        }
    }

    /**
     * 关闭整个EntityManagerFactory不能再使用它来创建EntityManager了
     */
    @Override
    public void shutdown() {
        getSessionFactory().close();
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

    public Object find(Class<?> cls, Object id) {
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

    public List findListByQuery(Class<?> cls, String queryString) {
        EntityManager em = getEntityManager();
        TypedQuery query = em.createQuery(queryString, cls);
        return query.getResultList();
    }

    public Object findFirstByQuery(Class<?> cls, String queryString) {
        EntityManager em = getEntityManager();
        TypedQuery query = em.createQuery(queryString, cls);
        query.setMaxResults(1);
        return ListUtil.first(query.getResultList());
    }

    @Override
    public Object findSingleByQuery(Class<?> cls, String sql) {
        Query query = getEntityManager().createQuery(sql, cls);
        query.setMaxResults(1);
        return query.getSingleResult();
    }

    /**
     * 直接执行原生SQL活得结果
     */
    @Override
    public List findListByRawQuery(Class<?> cls, String queryString) {
        EntityManager em = getEntityManager();
        javax.persistence.Query query = em.createNativeQuery(queryString, cls);
        return query.getResultList();
    }

    public List findListByRawQuery(String queryString) {
        EntityManager em = getEntityManager();
        javax.persistence.Query query = em.createNativeQuery(queryString);
        return query.getResultList();
    }

    public Object findFirstByRawQuery(Class<?> cls, String queryString) {
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

    public Object findSingleByNativeSql(Class<?> cls, String sql) {
        Query query = getEntityManager().createNativeQuery(sql, cls);
        query.setMaxResults(1);
        return query.getSingleResult();
    }

    public Object findSingleByNativeSql(String sql) {
        Query query = getEntityManager().createNativeQuery(sql);
        query.setMaxResults(1);
        return query.getSingleResult();
    }

    @Override
    public IWrappedQuery createQuery(Class<?> cls, String queryString) {
        return new EntityTypedQuery(getEntityManager().createQuery(queryString, cls));
    }

    @Override
    public IWrappedQuery createQuery(String queryString) {
        return new EntityQuery(getEntityManager().createQuery(queryString));
    }
}