package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.AbstractSession;
import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.core.Transaction;
import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.jdbcorm.ModelMeta;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zoowii on 15/1/26.
 */
public class JdbcSession extends AbstractSession {

    private java.sql.Connection jdbcConnection;
    private JdbcSessionFactory jdbcSessionFactory;
    private AtomicBoolean activeFlag = new AtomicBoolean(false);

    public AtomicBoolean getActiveFlag() {
        return activeFlag;
    }

    public JdbcSession(Connection jdbcConnection) {
        this.jdbcConnection = jdbcConnection;
    }

    public JdbcSession(JdbcSessionFactory jdbcSessionFactory) {
        this.jdbcSessionFactory = jdbcSessionFactory;
    }

    public synchronized java.sql.Connection getJdbcConnection() {
        if (jdbcConnection == null) {
            jdbcConnection = jdbcSessionFactory.createJdbcConnection();
        }
        return jdbcConnection;
    }

    public void setAutoCommit(boolean autoCommit) {
        try {
            getJdbcConnection().setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    public boolean getAutoCommit() {
        try {
            return getJdbcConnection().getAutoCommit();
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public Transaction getTransaction() {
        return new JdbcTransaction(this);
    }

    @Override
    public boolean isOpen() {
        try {
            return !getJdbcConnection().isClosed();
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            getJdbcConnection().close();
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
        if (jdbcSessionFactory != null) {
            jdbcSessionFactory.close();
        }
    }

    public ModelMeta getEntityMetaOfClass(Class<?> entityCls){
        // TODO: 根据数据库类型产生不同的SqlColumnTypeMapper
        // TODO: cache
        return new ModelMeta(entityCls);
    }

    @Override
    public void save(Object entity) {
        ModelMeta modelMeta=getEntityMetaOfClass(entity.getClass());
        // TODO
    }

    @Override
    public void update(Object entity) {
        // TODO
    }

    @Override
    public void merge(Object entity) {
        // TODO
    }

    @Override
    public void refresh(Object entity) {
        // TODO
    }

    @Override
    public Object find(Class<?> cls, Object id) {
        // TODO
        return null;
    }

    @Override
    public void delete(Object entity) {
        // TODO
    }

    @Override
    public void flush() {
        // TODO
    }

    @Override
    public int executeNativeSql(String sql) {
        // TODO
        return 0;
    }

    @Override
    public int executeQuerySql(String sql) {
        // TODO
        return 0;
    }

    @Override
    public List findListByQuery(Class<?> cls, String queryString) {
        // TODO
        return null;
    }

    @Override
    public Object findFirstByQuery(Class<?> cls, String queryString) {
        // TODO
        return null;
    }

    @Override
    public Object findSingleByQuery(Class<?> cls, String sql) {
        // TODO
        return null;
    }

    @Override
    public List findListByRawQuery(Class<?> cls, String queryString) {
        // TODO
        return null;
    }

    @Override
    public List findListByRawQuery(String queryString) {
        // TODO
        return null;
    }

    @Override
    public Object findFirstByRawQuery(Class<?> cls, String queryString) {
        // TODO
        return null;
    }

    @Override
    public Object findFirstByRawQuery(String queryString) {
        // TODO
        return null;
    }

    @Override
    public Object findSingleByNativeSql(Class<?> cls, String sql) {
        // TODO
        return null;
    }

    @Override
    public Object findSingleByNativeSql(String sql) {
        // TODO
        return null;
    }

    @Override
    public IWrappedQuery createQuery(Class<?> cls, String queryString) {
        // TODO
        return null;
    }

    @Override
    public IWrappedQuery createQuery(String queryString) {
        // TODO
        return null;
    }
}
