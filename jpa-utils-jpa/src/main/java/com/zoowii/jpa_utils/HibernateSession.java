package com.zoowii.jpa_utils;

import com.zoowii.jpa_utils.core.AbstractSession;
import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.core.IWrappedTypedQuery;
import com.zoowii.jpa_utils.core.Transaction;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.ORMSqlMapper;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
import com.zoowii.jpa_utils.query.ParameterBindings;
import com.zoowii.jpa_utils.util.ListUtil;
import org.hibernate.Query;
import org.hibernate.SQLQuery;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by zoowii on 14-12-23.
 */
public class HibernateSession extends AbstractSession {
    protected final HibernateSessionFactory hibernateSessionFactory;
    protected final org.hibernate.Session hibernateSession;
    protected SqlMapper sqlMapper = new ORMSqlMapper();

    @Override
    public int getIndexParamBaseOrdinal() {
        return 0;
    }

    public HibernateSession(HibernateSessionFactory sessionFactory) {
        hibernateSessionFactory = sessionFactory;
        hibernateSession = hibernateSessionFactory.getHibernateOriginSessionFactory().openSession();
    }

    public HibernateSession(org.hibernate.Session hibernateSession) {
        this.hibernateSession = hibernateSession;
        hibernateSessionFactory = null;
    }

    @Override
    public SqlMapper getSqlMapper() {
        return sqlMapper;
    }

    public Transaction getTransaction() {
        return new HibernateSessionTransaction(hibernateSession.getTransaction());
    }

    @Override
    public boolean isOpen() {
        return hibernateSession.isOpen();
    }

    @Override
    public void close() {
        hibernateSession.close();
    }

    @Override
    public void shutdown() {
        if (hibernateSessionFactory != null) {
            hibernateSessionFactory.close();
        }
    }

    @Override
    public void save(Object entity) {
        hibernateSession.save(entity);
    }

    @Override
    public void update(Object entity) {
        hibernateSession.update(entity);
    }

    @Override
    public void merge(Object entity) {
        hibernateSession.merge(entity);
    }

    @Override
    public void detach(Object entity) {
        hibernateSession.evict(entity);
    }

    public void clear() {
        hibernateSession.clear();
    }
    
    @Override
    public void refresh(Object entity) {
        hibernateSession.refresh(entity);
    }

    @Override
    public Object find(Class<?> cls, Object id) {
        return hibernateSession.get(cls, (Serializable) id);
    }

    @Override
    public void delete(Object entity) {
        hibernateSession.delete(entity);
    }

    @Override
    public void flush() {
        hibernateSession.flush();
    }

    @Override
    public int executeNativeSql(String sql) {
        return executeNativeSql(sql, null);
    }

    @Override
    public int executeNativeSql(String sql, ParameterBindings parameterBindings) {
        Query query = hibernateSession.createSQLQuery(sql);
        if(parameterBindings != null) {
            List<Object> indexedBindings = parameterBindings.getIndexBindings();
            for (int i = 0; i < indexedBindings.size();++i) {
                query.setParameter(i + 1, indexedBindings.get(i));
            }
            Map<String, Object> namedBindings = parameterBindings.getMapBindings();
            for(String key : namedBindings.keySet()) {
                Object value = namedBindings.get(key);
                if(value instanceof Collection) {
                    query.setParameterList(key, (Collection<?>)value);
                } else {
                    query.setParameter(key, value);
                }
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
        Query query = hibernateSession.createQuery(sql);
        if(parameterBindings != null) {
            List<Object> indexedBindings = parameterBindings.getIndexBindings();
            for (int i = 0; i < indexedBindings.size();++i) {
                query.setParameter(i + getIndexParamBaseOrdinal(), indexedBindings.get(i));
            }
            Map<String, Object> namedBindings = parameterBindings.getMapBindings();
            for(String key : namedBindings.keySet()) {
                Object value = namedBindings.get(key);
                if(value instanceof Collection) {
                    query.setParameterList(key, (Collection<?>)value);
                } else {
                    query.setParameter(key, value);
                }
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
        Query query = hibernateSession.createQuery(queryString);
        if(parameterBindings != null) {
            List<Object> indexedBindings = parameterBindings.getIndexBindings();
            for (int i = 0; i < indexedBindings.size();++i) {
                query.setParameter(i + getIndexParamBaseOrdinal(), indexedBindings.get(i));
            }
            Map<String, Object> namedBindings = parameterBindings.getMapBindings();
            for(String key : namedBindings.keySet()) {
                Object value = namedBindings.get(key);
                if(value instanceof Collection) {
                    query.setParameterList(key, (Collection<?>)value);
                } else {
                    query.setParameter(key, value);
                }
            }
        }
        return query.list();
    }

    @Override
    public Object findFirstByQuery(Class<?> cls, String queryString) {
        Query query = hibernateSession.createQuery(queryString);
        query.setMaxResults(1);
        return ListUtil.first(query.list());
    }

    @Override
    public Object findSingleByQuery(Class<?> cls, String sql) {
        Query query = hibernateSession.createQuery(sql);
        query.setMaxResults(1);
        return query.uniqueResult();
    }

    @Override
    public List findListByRawQuery(Class<?> cls, String queryString) {
        return findListByRawQuery(cls, queryString, null);
    }

    @Override
    public List findListByRawQuery(Class<?> cls, String queryString, ParameterBindings parameterBindings) {
        SQLQuery query = hibernateSession.createSQLQuery(queryString);
        query.addEntity(cls);
        if(parameterBindings != null) {
            List<Object> indexedBindings = parameterBindings.getIndexBindings();
            for (int i = 0; i < indexedBindings.size();++i) {
                query.setParameter(i, indexedBindings.get(i));
            }
            Map<String, Object> namedBindings = parameterBindings.getMapBindings();
            for(String key : namedBindings.keySet()) {
                Object value = namedBindings.get(key);
                if(value instanceof Collection) {
                    query.setParameterList(key, (Collection<?>)value);
                } else {
                    query.setParameter(key, value);
                }
            }
        }
        return query.list();
    }

    @Override
    public List findListByRawQuery(String queryString) {
        SQLQuery query = hibernateSession.createSQLQuery(queryString);
        return query.list();
    }

    @Override
    public Object findFirstByRawQuery(Class<?> cls, String queryString) {
        return findFirstByRawQuery(cls, queryString, null);
    }

    @Override
    public Object findFirstByRawQuery(Class<?> cls, String queryString, ParameterBindings parameterBindings) {
        SQLQuery query = hibernateSession.createSQLQuery(queryString);
        query.addEntity(cls);
        query.setMaxResults(1);
        return ListUtil.first(query.list());
    }

    @Override
    public Object findFirstByRawQuery(String queryString) {
        SQLQuery query = hibernateSession.createSQLQuery(queryString);
        query.setMaxResults(1);
        return ListUtil.first(query.list());
    }

    @Override
    public Object findSingleByRawSql(Class<?> cls, String sql) {
        return findSingleByRawSql(cls, sql, null);
    }

    @Override
    public Object findSingleByRawSql(Class<?> cls, String sql, ParameterBindings parameterBindings) {
        SQLQuery query = hibernateSession.createSQLQuery(sql);
        if(parameterBindings != null) {
            List<Object> indexedBindings = parameterBindings.getIndexBindings();
            for (int i = 0; i < indexedBindings.size();++i) {
                query.setParameter(i, indexedBindings.get(i));
            }
            Map<String, Object> namedBindings = parameterBindings.getMapBindings();
            for(String key : namedBindings.keySet()) {
                Object value = namedBindings.get(key);
                if(value instanceof Collection) {
                    query.setParameterList(key, (Collection<?>)value);
                } else {
                    query.setParameter(key, value);
                }
            }
        }
        query.addEntity(cls);
        query.setMaxResults(1);
        return query.uniqueResult();
    }

    @Override
    public Object findSingleByRawSql(String sql) {
        SQLQuery query = hibernateSession.createSQLQuery(sql);
        query.setMaxResults(1);
        return query.uniqueResult();
    }

    @Override
    public IWrappedTypedQuery createQuery(Class<?> cls, String queryString) {
        return new HibernateQuery(hibernateSession.createQuery(queryString));
    }

    @Override
    public IWrappedQuery createQuery(String queryString) {
        return new HibernateQuery(hibernateSession.createQuery(queryString));
    }
}
