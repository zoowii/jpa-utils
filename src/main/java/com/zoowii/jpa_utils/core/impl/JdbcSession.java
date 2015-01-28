package com.zoowii.jpa_utils.core.impl;

import com.google.common.base.Function;
import com.zoowii.jpa_utils.core.AbstractSession;
import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.core.Transaction;
import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.jdbcorm.SqlStatementInfo;
import com.zoowii.jpa_utils.query.ParameterBindings;
import com.zoowii.jpa_utils.util.FieldAccessor;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.exception.CloneFailedException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO: 支持类似MyBatis的执行SQL的方法
 * TODO: 把SQL的构造整合到query/Expr包的通用构造方案中
 * Created by zoowii on 15/1/26.
 */
public class JdbcSession extends AbstractSession {
    private static final Logger LOG = Logger.getLogger(JdbcSession.class);

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

    public ModelMeta getEntityMetaOfClass(Class<?> entityCls) {
        // TODO: 根据数据库类型产生不同的SqlColumnTypeMapper
        // TODO: cache
        return new ModelMeta(entityCls);
    }

    private PreparedStatement prepareStatement(String sql) {
        try {
            return getJdbcConnection().prepareStatement(sql);
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    private int executePrepareStatementUpdate(PreparedStatement pstm) {
        try {
            return pstm.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    /**
     * TODO: 考虑支持通过一些类似@Insert("... ${column}..."),@Update, @Delete, @Query的注解来自定义查询
     *
     * @param entity
     */
    @Override
    public void save(Object entity) {
        try {
            ModelMeta modelMeta = getEntityMetaOfClass(entity.getClass());
            SqlStatementInfo insertSqlInfo = modelMeta.getSqlMapper().getInsert(modelMeta, entity);
            LOG.info("insert sql: " + insertSqlInfo.getSql());
            PreparedStatement pstm = getJdbcConnection().prepareStatement(insertSqlInfo.getSql(), java.sql.Statement.RETURN_GENERATED_KEYS);
            ParameterBindings parameterBindings = insertSqlInfo.getParameterBindings();
            parameterBindings.applyToPrepareStatement(pstm);
            int changedCount = pstm.executeUpdate();
            if (changedCount < 1) {
                throw new JdbcRuntimeException("No record affected when save entity");
            }
            FieldAccessor idAccessor = modelMeta.getIdAccessor();
            if (idAccessor != null && idAccessor.getProperty(entity) == null) {
                ResultSet generatedKeysResultSet = pstm.getGeneratedKeys();
                try {
                    if (generatedKeysResultSet.next()) {
                        Object generatedId = generatedKeysResultSet.getObject(1);
                        idAccessor.setProperty(entity, generatedId);
                        // TODO: reflesh entity properties
                    }
                } finally {
                    generatedKeysResultSet.close();
                }
            }
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public void update(final Object entity) {
        try {
            final ModelMeta modelMeta = getEntityMetaOfClass(entity.getClass());
            final FieldAccessor idAccessor = modelMeta.getIdAccessor();
            SqlStatementInfo updateSqlInfo = modelMeta.getSqlMapper().getUpdate(modelMeta, entity, new Function<ParameterBindings, String>() {
                @Override
                public String apply(ParameterBindings parameterBindings) {
                    return modelMeta.getSqlMapper().getIdEqConditionSubSql(modelMeta, idAccessor.getProperty(entity), parameterBindings, null);
                }
            });
            LOG.info("update sql: " + updateSqlInfo.getSql());
            QueryRunner runner = new QueryRunner();
            runner.update(getJdbcConnection(), updateSqlInfo.getSql(), updateSqlInfo.getParameterBindings().getIndexParametersArray());
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public void merge(Object entity) {

    }

    @Override
    public void refresh(Object entity) {
        ModelMeta modelMeta = getEntityMetaOfClass(entity.getClass());
        FieldAccessor idAccessor = modelMeta.getIdAccessor();
        Object latestEntity = find(entity.getClass(), idAccessor.getProperty(entity));
        if (latestEntity != null) {
            try {
                BeanUtils.copyProperties(entity, latestEntity);
            } catch (IllegalAccessException e) {
                throw new CloneFailedException(e);
            } catch (InvocationTargetException e) {
                throw new CloneFailedException(e);
            }
        } else {
            throw new JdbcRuntimeException("Can't find the related record of entity " + entity);
        }
    }

    private ResultSetHandler<List<Object>> getListResultSetHandler(Class<?> cls) {
        return new BeanListHandler(cls); // TODO: 使用modelMeta来自定义row processor
    }

    private ResultSetHandler<Object> getRowBeanResultSetHandler(Class<?> cls) {
        return new BeanHandler(cls); // TODO: 使用modelMeta来自定义row processor
    }

    /**
     * TODO: 改成使用通用的query模型
     *
     * @param cls
     * @param id
     * @return
     */
    @Override
    public Object find(Class<?> cls, Object id) {
        try {
            ModelMeta modelMeta = getEntityMetaOfClass(cls);
            ParameterBindings parameterBindings = new ParameterBindings();
            Pair<String, String> fromPair = modelMeta.getSqlMapper().getFromSubSql(modelMeta, true);
            String fromSql = fromPair.getLeft();
            String tableAlias = fromPair.getRight();
            String columnsSql = modelMeta.getSqlMapper().getColumnsSql(modelMeta, tableAlias, true);
            String selectSql = modelMeta.getSqlMapper().getSelectSubSql(columnsSql);
            String conditionSql = modelMeta.getSqlMapper().getIdEqConditionSubSql(modelMeta, id, parameterBindings, tableAlias);
            String whereSql = modelMeta.getSqlMapper().getWhereSubSql(conditionSql);
            String sql = selectSql + fromSql + whereSql;
            LOG.info("query sql " + sql);
            QueryRunner runner = new QueryRunner();
            ResultSetHandler<List<Object>> handler = getListResultSetHandler(cls);
            List<Object> result = runner.query(getJdbcConnection(), sql, handler, parameterBindings.getIndexParametersArray());
            if (result.size() > 0) {
                return result.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public void delete(Object entity) {
        try {
            ModelMeta modelMeta = getEntityMetaOfClass(entity.getClass());
            ParameterBindings parameterBindings = new ParameterBindings();
            Pair<String, String> fromPair = modelMeta.getSqlMapper().getFromSubSql(modelMeta, false);
            String fromSql = fromPair.getLeft();
            String tableAlias = fromPair.getRight();
            FieldAccessor idAccessor = modelMeta.getIdAccessor();
            String conditionSql = modelMeta.getSqlMapper().getIdEqConditionSubSql(modelMeta, idAccessor.getProperty(entity), parameterBindings, tableAlias);
            String whereSql = modelMeta.getSqlMapper().getWhereSubSql(conditionSql);
            String sql = modelMeta.getSqlMapper().getDeleteSubSql(fromSql, whereSql);
            LOG.info("delete sql: " + sql);
            QueryRunner runner = new QueryRunner();
            runner.update(getJdbcConnection(), sql, parameterBindings.getIndexParametersArray());
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public int executeNativeSql(String sql) {
        return executeNativeSql(sql, null);
    }

    @Override
    public int executeQuerySql(String sql) {
        return executeNativeSql(sql);
    }

    @Override
    public List findListByQuery(Class<?> cls, String queryString) {
        return findListByRawQuery(cls, queryString);
    }

    @Override
    public Object findFirstByQuery(Class<?> cls, String queryString) {
        return findFirstByRawQuery(cls, queryString);
    }

    @Override
    public Object findSingleByQuery(Class<?> cls, String sql) {
        return findSingleByNativeSql(cls, sql);
    }

    @Override
    public List findListByRawQuery(Class<?> cls, String queryString) {
        try {
            QueryRunner runner = new QueryRunner();
            ResultSetHandler<List<Object>> handler = getListResultSetHandler(cls);
            return runner.query(getJdbcConnection(), queryString, handler);
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public List findListByRawQuery(String queryString) {
        throw new JdbcRuntimeException("need a bean class");
    }

    @Override
    public Object findFirstByRawQuery(Class<?> cls, String queryString) {
        try {
            QueryRunner runner = new QueryRunner();
            ResultSetHandler<List<Object>> handler = getListResultSetHandler(cls);
            List<Object> result = runner.query(getJdbcConnection(), queryString, handler);
            if (result.size() == 1) {
                return result.get(0);
            } else {
                throw new JdbcRuntimeException("There are not single results when executing " + queryString);
            }
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public Object findFirstByRawQuery(String queryString) {
        throw new NotImplementedException("need a bean class");
    }

    @Override
    public Object findSingleByNativeSql(Class<?> cls, String sql) {
        try {
            QueryRunner runner = new QueryRunner();
            ResultSetHandler<List<Object>> handler = getListResultSetHandler(cls);
            List<Object> result = runner.query(getJdbcConnection(), sql, handler);
            if (result.size() == 1) {
                return result.get(0);
            } else {
                throw new JdbcRuntimeException("There are not single results when executing " + sql);
            }
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public Object findSingleByNativeSql(String sql) {
        throw new NotImplementedException("need a bean class");
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

    public int executeNativeSql(String sql, ParameterBindings parameterBindings) {
        PreparedStatement pstm = prepareStatement(sql);
        if (parameterBindings != null) {
            parameterBindings.applyToPrepareStatement(pstm);
        }
        return executePrepareStatementUpdate(pstm);
    }
}
