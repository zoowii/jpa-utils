package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.AbstractSession;
import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.core.IWrappedTypedQuery;
import com.zoowii.jpa_utils.core.Transaction;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.ORMSqlMapper;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
import com.zoowii.jpa_utils.util.ListUtil;
import org.hibernate.Query;
import org.hibernate.SQLQuery;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zoowii on 14-12-23.
 */
public class HibernateSession extends AbstractSession {
    protected final HibernateSessionFactory hibernateSessionFactory;
    protected final org.hibernate.Session hibernateSession;
    protected SqlMapper sqlMapper = new ORMSqlMapper();

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
        return hibernateSession.createSQLQuery(sql).executeUpdate();
    }

    @Override
    public int executeQuerySql(String sql) {
        return hibernateSession.createQuery(sql).executeUpdate();
    }

    @Override
    public List findListByQuery(Class<?> cls, String queryString) {
        return hibernateSession.createQuery(queryString).list();
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
        SQLQuery query = hibernateSession.createSQLQuery(queryString);
        query.addEntity(cls);
        return query.list();
    }

    @Override
    public List findListByRawQuery(String queryString) {
        SQLQuery query = hibernateSession.createSQLQuery(queryString);
        return query.list();
    }

    @Override
    public Object findFirstByRawQuery(Class<?> cls, String queryString) {
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
    public Object findSingleByNativeSql(Class<?> cls, String sql) {
        SQLQuery query = hibernateSession.createSQLQuery(sql);
        query.addEntity(cls);
        query.setMaxResults(1);
        return query.uniqueResult();
    }

    @Override
    public Object findSingleByNativeSql(String sql) {
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
