package com.zoowii.jpa_utils.core;

import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
import com.zoowii.jpa_utils.query.Expr;
import com.zoowii.jpa_utils.query.ParameterBindings;

import java.util.List;

/**
 * Created by zoowii on 14-12-23.
 */
public interface Session {

    SqlMapper getSqlMapper();

    ModelMeta getEntityMetaOfClass(Class<?> entityCls);

    Transaction getTransaction();

    void begin();

    void commit();

    boolean isOpen();

    void rollback();

    boolean isClosed();

    void close();

    /**
     * close all session factory so you can't use it to create session again
     */
    void shutdown();

    /**
     * @return transaction nested depth
     */
    int getTransactionNestedLevel();

    boolean isTransactionActive();

    void save(Object entity);

    void update(Object entity);

    void merge(Object entity);

    void detach(Object entity);

    void refresh(Object entity);
    /**
     * clear cache
     */
    void clear();

    Object find(Class<?> cls, Object id);

    void delete(Object entity);

    /**
     * 删除满足条件的model的记录
     *
     * @param model
     * @param expr
     * @return
     */
    int delete(Class<?> model, Expr expr);

    void flush();

    int executeNativeSql(String sql);

    int executeQuerySql(String sql);

    int executeQuerySql(String sql, ParameterBindings parameterBindings);

    List findListByQuery(Class<?> cls, String queryString);

    Object findFirstByQuery(Class<?> cls, String queryString);

    Object findSingleByQuery(Class<?> cls, String sql);

    /**
     * 直接执行原生SQL活得结果
     */
    List findListByRawQuery(Class<?> cls, String queryString);

    List findListByRawQuery(String queryString);

    Object findFirstByRawQuery(Class<?> cls, String queryString);

    Object findFirstByRawQuery(String queryString);

    Object findSingleByNativeSql(Class<?> cls, String sql);

    Object findSingleByNativeSql(String sql);

    IWrappedQuery createQuery(Class<?> cls, String queryString);

    IWrappedQuery createQuery(String queryString);

    Session asThreadLocal();
}
