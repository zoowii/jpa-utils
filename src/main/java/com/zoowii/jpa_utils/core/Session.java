package com.zoowii.jpa_utils.core;

import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
import com.zoowii.jpa_utils.query.Expr;

import java.util.List;

/**
 * Created by zoowii on 14-12-23.
 */
public interface Session {

    public SqlMapper getSqlMapper();

    public ModelMeta getEntityMetaOfClass(Class<?> entityCls);

    public Transaction getTransaction();

    public void begin();

    public void commit();

    public boolean isOpen();

    public void rollback();

    public boolean isClosed();

    public void close();

    /**
     * 关闭整个EntityManagerFactory不能再使用它来创建EntityManager了
     */
    public void shutdown();

    /**
     * 获取事务嵌套层数
     */
    public int getTransactionNestedLevel();

    public boolean isTransactionActive();

    public void save(Object entity);

    public void update(Object entity);

    public void merge(Object entity);

    public void refresh(Object entity);

    /**
     * clear cache
     */
    public void clear();

    public Object find(Class<?> cls, Object id);

    public void delete(Object entity);

    /**
     * 删除满足条件的model的记录
     *
     * @param model
     * @param expr
     * @return
     */
    public int delete(Class<?> model, Expr expr);

    public void flush();

    public int executeNativeSql(String sql);

    public int executeQuerySql(String sql);

    public List findListByQuery(Class<?> cls, String queryString);

    public Object findFirstByQuery(Class<?> cls, String queryString);

    public Object findSingleByQuery(Class<?> cls, String sql);

    /**
     * 直接执行原生SQL活得结果
     */
    public List findListByRawQuery(Class<?> cls, String queryString);

    public List findListByRawQuery(String queryString);

    public Object findFirstByRawQuery(Class<?> cls, String queryString);

    public Object findFirstByRawQuery(String queryString);

    public Object findSingleByNativeSql(Class<?> cls, String sql);

    public Object findSingleByNativeSql(String sql);

    public IWrappedQuery createQuery(Class<?> cls, String queryString);

    public IWrappedQuery createQuery(String queryString);

    public Session asThreadLocal();
}
