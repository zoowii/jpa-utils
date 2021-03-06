package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.*;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.ORMSqlMapper;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
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
    protected SqlMapper sqlMapper = new ORMSqlMapper();

    protected EntitySessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = EntitySessionFactory.getDefaultEntitySessionFactory();
        }
        return sessionFactory;
    }

    protected EntityManagerFactory getEntityManagerFactory() {
        return getSessionFactory().getEntityManagerFactory();
    }

    protected EntitySession() {
    }

    public EntitySession(EntityManager entityManager) {
        this.em = entityManager;
    }

    public EntitySession(EntitySessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public synchronized EntityManager getEntityManager() {
        if (em == null) {
            em = getEntityManagerFactory().createEntityManager();
        }
        return em;
    }

    @Override
    public SqlMapper getSqlMapper() {
        return sqlMapper;
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
     * the EntityManagerFactory's connection cleanup thread will clean it
     */
    @Override
    public void close() {
        if (isOpen()) {
            getEntityManager().close();
        }
    }

    @Override
    public void shutdown() {
        getSessionFactory().close();
    }

    @Override
    public void save(Object entity) {
        getEntityManager().persist(entity);
    }

    @Override
    public void update(Object entity) {
        getEntityManager().persist(entity);
        flush();
        clear();
    }

    @Override
    public void merge(Object entity) {
        getEntityManager().merge(entity);
    }

    @Override
    public void detach(Object entity) {
        getEntityManager().detach(entity);
    }

    @Override
    public void refresh(Object entity) {
        getEntityManager().refresh(entity);
    }

    @Override
    public Object find(Class<?> cls, Object id) {
        return getEntityManager().find(cls, id);
    }

    @Override
    public void clear() {
        getEntityManager().clear();
    }

    @Override
    public void delete(Object entity) {
        getEntityManager().remove(entity);
    }

    @Override
    public void flush() {
        getEntityManager().flush();
    }

    @Override
    public int executeNativeSql(String sql) {
        return executeNativeSql(sql, null);
    }

    @Override
    public int executeNativeSql(String sql, ParameterBindings parameterBindings) {
        Query query = getEntityManager().createNativeQuery(sql);
        if (parameterBindings != null) {
            List<Object> indexedBindings = parameterBindings.getIndexBindings();
            for (int i = 0; i < indexedBindings.size(); ++i) {
                query.setParameter(i + 1, indexedBindings.get(i));
            }
            Map<String, Object> namedBindings = parameterBindings.getMapBindings();
            for (String key : namedBindings.keySet()) {
                query.setParameter(key, namedBindings.get(key));
            }
        }
        return query.executeUpdate();
    }

    @Override
    public int executeQuerySql(String sql) {
        return executeQuerySql(sql, null);
    }

    @Override
    public int executeQuerySql(String sql, ParameterBindings parameterBindings) {
        Query query = getEntityManager().createQuery(sql);
        if (parameterBindings != null) {
            List<Object> indexedBindings = parameterBindings.getIndexBindings();
            for (int i = 0; i < indexedBindings.size(); ++i) {
                query.setParameter(i + 1, indexedBindings.get(i));
            }
            Map<String, Object> namedBindings = parameterBindings.getMapBindings();
            for (String key : namedBindings.keySet()) {
                query.setParameter(key, namedBindings.get(key));
            }
        }
        return query.executeUpdate();
    }

    @Override
    public List findListByQuery(Class<?> cls, String queryString) {
        return findListByQuery(cls, queryString, null);
    }

    @Override
    public List findListByQuery(Class<?> cls, String queryString, ParameterBindings parameterBindings) {
        EntityManager em = getEntityManager();
        TypedQuery query = em.createQuery(queryString, cls);
        if(parameterBindings != null) {
            List<Object> indexedBindings = parameterBindings.getIndexBindings();
            for (int i = 0; i < indexedBindings.size(); ++i) {
                query.setParameter(i + this.getIndexParamBaseOrdinal(), indexedBindings.get(i));
            }
            Map<String, Object> namedBindings = parameterBindings.getMapBindings();
            for (String key : namedBindings.keySet()) {
                query.setParameter(key, namedBindings.get(key));
            }
        }
        return query.getResultList();
    }

    @Override
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

    @Override
    public List findListByRawQuery(Class<?> cls, String queryString) {
        return findListByRawQuery(cls, queryString, null);
    }

    @Override
    public List findListByRawQuery(Class<?> cls, String queryString, ParameterBindings parameterBindings) {
        EntityManager em = getEntityManager();
        javax.persistence.Query query = em.createNativeQuery(queryString, cls);
        if (parameterBindings != null) {
            List<Object> indexedBindings = parameterBindings.getIndexBindings();
            for (int i = 0; i < indexedBindings.size(); ++i) {
                query.setParameter(i + 1, indexedBindings.get(i));
            }
            Map<String, Object> namedBindings = parameterBindings.getMapBindings();
            for (String key : namedBindings.keySet()) {
                query.setParameter(key, namedBindings.get(key));
            }
        }
        return query.getResultList();
    }

    @Override
    public List findListByRawQuery(String queryString) {
        EntityManager em = getEntityManager();
        javax.persistence.Query query = em.createNativeQuery(queryString);
        return query.getResultList();
    }

    @Override
    public Object findFirstByRawQuery(Class<?> cls, String queryString) {
        return findFirstByRawQuery(cls, queryString, null);
    }

    @Override
    public Object findFirstByRawQuery(Class<?> cls, String queryString, ParameterBindings parameterBindings) {
        EntityManager em = getEntityManager();
        javax.persistence.Query query = em.createNativeQuery(queryString, cls);
        if (parameterBindings != null) {
            List<Object> indexedBindings = parameterBindings.getIndexBindings();
            for (int i = 0; i < indexedBindings.size(); ++i) {
                query.setParameter(i + 1, indexedBindings.get(i));
            }
            Map<String, Object> namedBindings = parameterBindings.getMapBindings();
            for (String key : namedBindings.keySet()) {
                query.setParameter(key, namedBindings.get(key));
            }
        }
        query.setMaxResults(1);
        return ListUtil.first(query.getResultList());
    }

    @Override
    public Object findFirstByRawQuery(String queryString) {
        EntityManager em = getEntityManager();
        javax.persistence.Query query = em.createNativeQuery(queryString);
        query.setMaxResults(1);
        return ListUtil.first(query.getResultList());
    }

    @Override
    public Object findSingleByRawSql(Class<?> cls, String sql) {
        return findSingleByRawSql(cls, sql, null);
    }

    @Override
    public Object findSingleByRawSql(Class<?> cls, String sql, ParameterBindings parameterBindings) {
        Query query = getEntityManager().createNativeQuery(sql, cls);
        if (parameterBindings != null) {
            List<Object> indexedBindings = parameterBindings.getIndexBindings();
            for (int i = 0; i < indexedBindings.size(); ++i) {
                query.setParameter(i + 1, indexedBindings.get(i));
            }
            Map<String, Object> namedBindings = parameterBindings.getMapBindings();
            for (String key : namedBindings.keySet()) {
                query.setParameter(key, namedBindings.get(key));
            }
        }
        query.setMaxResults(1);
        return query.getSingleResult();
    }

    @Override
    public Object findSingleByRawSql(String sql) {
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
