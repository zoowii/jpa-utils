package com.zoowii.jpa_utils.core.impl;

import com.google.common.base.Function;
import com.zoowii.jpa_utils.core.AbstractSession;
import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.core.Transaction;
import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.jdbcorm.NamedParameterStatement;
import com.zoowii.jpa_utils.jdbcorm.SqlStatementInfo;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.MySQLMapper;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
import com.zoowii.jpa_utils.orm.Model;
import com.zoowii.jpa_utils.query.Expr;
import com.zoowii.jpa_utils.query.ParameterBindings;
import com.zoowii.jpa_utils.query.Query;
import com.zoowii.jpa_utils.query.QueryInfo;
import com.zoowii.jpa_utils.util.FieldAccessor;
import com.zoowii.jpa_utils.util.Logger;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.exception.CloneFailedException;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO: support methods like MyBatis
 * Created by zoowii on 15/1/26.
 */
public class JdbcSession extends AbstractSession {
    private java.sql.Connection jdbcConnection;
    private JdbcSessionFactory jdbcSessionFactory;
    private AtomicBoolean activeFlag = new AtomicBoolean(false);
    private SqlMapper sqlMapper = new MySQLMapper();
    private transient boolean isInBatch = false;
    private transient NamedParameterStatement batchPreparedStatement;
    private transient boolean closed = false;

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

    public void setSqlMapper(SqlMapper sqlMapper) {
        this.sqlMapper = sqlMapper;
    }

    @Override
    public SqlMapper getSqlMapper() {
        return sqlMapper;
    }

    @Override
    public Transaction getTransaction() {
        return new JdbcTransaction(this);
    }

    @Override
    public boolean isOpen() {
        try {
            if(closed) {
                return false;
            }
            if(jdbcConnection==null) {
                return true;
            }
            return !jdbcConnection.isClosed();
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (getTransactionNestedLevel() > 0) {
            return;
        }
        try {
            if(closed) {
                return;
            }
            if(jdbcConnection!=null) {
                jdbcConnection.close();
            }
            closed = true;
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

    private PreparedStatement prepareStatement(String sql) {
        try {
            Logger.logSql("execute sql:" + sql);
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
     * TODO: may add feature like @Insert("... ${column}..."),@Update, @Delete, @Query to define query
     *
     * @param entity entity to save
     */
    @Override
    public void save(Object entity) {
        try {
            ModelMeta modelMeta = getEntityMetaOfClass(entity.getClass());
            SqlStatementInfo insertSqlInfo = modelMeta.getSqlMapper().getInsert(modelMeta, entity);
            Logger.logSql("insert sql: " + insertSqlInfo.getSql());
            if (!isInBatch) {
                NamedParameterStatement pstm = new NamedParameterStatement(getJdbcConnection(), insertSqlInfo.getSql(), java.sql.Statement.RETURN_GENERATED_KEYS);
                try {
                    ParameterBindings parameterBindings = insertSqlInfo.getParameterBindings();
                    parameterBindings.applyToNamedPrepareStatement(pstm);
                    int changedCount = pstm.executeUpdate();
                    if (changedCount < 1) {
                        throw new JdbcRuntimeException("No record affected when save entity");
                    }
                    FieldAccessor idAccessor = modelMeta.getIdAccessor();
                    if (idAccessor != null && idAccessor.getProperty(entity) == null) {
                        ResultSet generatedKeysResultSet = pstm.getStatement().getGeneratedKeys();
                        try {
                            if (generatedKeysResultSet.next()) {
                                Object generatedId = generatedKeysResultSet.getObject(1);
                                idAccessor.setProperty(entity, generatedId);
                                refresh(entity);
                            }
                        } finally {
                            generatedKeysResultSet.close();
                        }
                    }
                } finally {
                    pstm.close();
                }
            } else {
                if (batchPreparedStatement == null) {
                    batchPreparedStatement = new NamedParameterStatement(getJdbcConnection(), insertSqlInfo.getSql());
                }
                ParameterBindings parameterBindings = insertSqlInfo.getParameterBindings();
                parameterBindings.applyToNamedPrepareStatement(batchPreparedStatement);
                batchPreparedStatement.addBatch();
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
            Logger.logSql("update sql: " + updateSqlInfo.getSql());
            if (!isInBatch) {
                NamedParameterStatement namedParameterStatement = new NamedParameterStatement(getJdbcConnection(), updateSqlInfo.getSql());
                try {
                    updateSqlInfo.getParameterBindings().applyToNamedPrepareStatement(namedParameterStatement);
                    namedParameterStatement.executeUpdate();
                } finally {
                    namedParameterStatement.close();
                }
            } else {
                if (batchPreparedStatement == null) {
                    batchPreparedStatement = new NamedParameterStatement(getJdbcConnection(), updateSqlInfo.getSql());
                }
                updateSqlInfo.getParameterBindings().applyToNamedPrepareStatement(batchPreparedStatement);
                batchPreparedStatement.addBatch();
            }
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public void startBatch() {
        this.isInBatch = true;
    }

    @Override
    public void endBatch() {
        this.isInBatch = false;
        if (this.batchPreparedStatement != null) {
            try {
                this.batchPreparedStatement.close();
            } catch (SQLException e) {
                throw new JdbcRuntimeException(e);
            }
        }
        this.batchPreparedStatement = null;
    }

    @Override
    public int[] executeBatch() {
        if (isInBatch && batchPreparedStatement != null) {
            try {
                return batchPreparedStatement.executeBatch();
            } catch (SQLException e) {
                throw new JdbcRuntimeException(e);
            }
        } else {
            throw new JdbcRuntimeException("Not in batch");
        }
    }

    @Override
    public void updateBatch(List<Object> entities) {
        startBatch();
        try {
            for (Object entity : entities) {
                update(entity);
            }
            executeBatch();
        } finally {
            endBatch();
        }
    }

    @Override
    public void saveBatch(List<Object> entities) {
        startBatch();
        try {
            for (Object entity : entities) {
                save(entity);
            }
            executeBatch();
        } finally {
            endBatch();
        }
    }

    @Override
    public void deleteBatch(List<Object> entities) {
        startBatch();
        try {
            for (Object entity : entities) {
                delete(entity);
            }
            executeBatch();
        } finally {
            endBatch();
        }
    }

    @Override
    public void merge(Object entity) {

    }
    
    @Override
    public void clear() {

    }

    @Override
    public void detach(Object entity) {

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

    public static ResultSetHandler<List<Object>> getListResultSetHandler(ModelMeta modelMeta) {
        return new JdbcOrmBeanListHandler(modelMeta.getModelCls(), modelMeta);
    }

    public static ResultSetHandler<Object> getRowBeanResultSetHandler(ModelMeta modelMeta) {
        return new JdbcOrmBeanHandler(modelMeta.getModelCls(), modelMeta);
    }

    /**
     * TODO: change to common query model
     *
     * @param cls model cls to find
     * @param id model primary key value
     * @return found record
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
            Logger.logSql("query sql " + sql);
            ResultSetHandler<List<Object>> handler = getListResultSetHandler(modelMeta);
            NamedParameterStatement namedParameterStatement = new NamedParameterStatement(getJdbcConnection(), sql);
            try {
                parameterBindings.applyToNamedPrepareStatement(namedParameterStatement);
                ResultSet resultSet = namedParameterStatement.executeQuery();
                try {
                    List<Object> result = handler.handle(resultSet);
                    if (result.size() > 0) {
                        return result.get(0);
                    } else {
                        return null;
                    }
                } finally {
                    resultSet.close();
                }
            } finally {
                namedParameterStatement.close();
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
            Logger.logSql("delete sql: " + sql);
            if (!isInBatch) {
                NamedParameterStatement namedParameterStatement = new NamedParameterStatement(getJdbcConnection(), sql);
                try {
                    parameterBindings.applyToNamedPrepareStatement(namedParameterStatement);
                    namedParameterStatement.executeUpdate();
                } finally {
                    namedParameterStatement.close();
                }
            } else {
                if (batchPreparedStatement == null) {
                    batchPreparedStatement = new NamedParameterStatement(getJdbcConnection(), sql);
                }
                parameterBindings.applyToNamedPrepareStatement(batchPreparedStatement);
                batchPreparedStatement.addBatch();
            }
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public int delete(Class<?> model, Expr expr) {
        try {
            ModelMeta modelMeta = getEntityMetaOfClass(model);
            SqlMapper sqlMapper = getSqlMapper();
            Pair<String, String> fromSqlPair = sqlMapper.getFromSubSql(modelMeta, false);
            Query query = new Query(model, Model.getSession());
            QueryInfo exprQuery = expr.toQueryString(sqlMapper, query);
            String sql = String.format("DELETE %s WHERE (%s)", fromSqlPair.getLeft(), exprQuery.getQueryString());
            Logger.info("delete sql: " + sql);
            ParameterBindings parameterBindings = exprQuery.getParameterBindings();
            if (!isInBatch) {
                NamedParameterStatement namedParameterStatement = new NamedParameterStatement(getJdbcConnection(), sql);
                try {
                    parameterBindings.applyToNamedPrepareStatement(namedParameterStatement);
                    return namedParameterStatement.executeUpdate();
                } finally {
                    namedParameterStatement.close();
                }
            } else {
                if (batchPreparedStatement == null) {
                    batchPreparedStatement = new NamedParameterStatement(getJdbcConnection(), sql);
                }
                parameterBindings.applyToNamedPrepareStatement(batchPreparedStatement);
                batchPreparedStatement.addBatch();
                return 0;
            }
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
        return executeQuerySql(sql, null);
    }

    @Override
    public List findListByQuery(Class<?> cls, String queryString) {
        return findListByQuery(cls, queryString, null);
    }

    @Override
    public List findListByQuery(Class<?> cls, String queryString, ParameterBindings parameterBindings) {
        return findListByRawQuery(cls, queryString, parameterBindings);
    }

    @Override
    public Object findFirstByQuery(Class<?> cls, String queryString) {
        return findFirstByRawQuery(cls, queryString);
    }

    @Override
    public Object findSingleByQuery(Class<?> cls, String sql) {
        return findSingleByRawSql(cls, sql);
    }

    @Override
    public List findListByRawQuery(Class<?> cls, String queryString) {
        return findListByRawQuery(cls, queryString, null);
    }

    @Override
    public List findListByRawQuery(Class<?> cls, String queryString, ParameterBindings parameterBindings) {
        try {
            QueryRunner runner = new QueryRunner();
            ResultSetHandler<List<Object>> handler = getListResultSetHandler(getEntityMetaOfClass(cls));
            Object[] params = parameterBindings != null ? parameterBindings.getIndexParametersArray() : new Object[0];
            Logger.logSql("query sql: " + queryString);
            return runner.query(getJdbcConnection(), queryString, handler, params);
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
        return findFirstByRawQuery(cls, queryString, null);
    }

    @Override
    public Object findFirstByRawQuery(Class<?> cls, String queryString, ParameterBindings parameterBindings) {
        try {
            QueryRunner runner = new QueryRunner();
            ResultSetHandler<List<Object>> handler = getListResultSetHandler(getEntityMetaOfClass(cls));
            Object[] params = parameterBindings != null ? parameterBindings.getIndexParametersArray() : new Object[0];
            Logger.logSql("query first sql: " + queryString);
            List<Object> result = runner.query(getJdbcConnection(), queryString, handler, params);
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
    public Object findSingleByRawSql(Class<?> cls, String sql) {
        return findSingleByRawSql(cls, sql, null);
    }

    @Override
    public Object findSingleByRawSql(Class<?> cls, String sql, ParameterBindings parameterBindings) {
        try {
            QueryRunner runner = new QueryRunner();
            ResultSetHandler<List<Object>> handler = getListResultSetHandler(getEntityMetaOfClass(cls));
            Object[] params = parameterBindings != null ? parameterBindings.getIndexParametersArray() : new Object[0];
            Logger.logSql("query single sql: " + sql);
            List<Object> result = runner.query(getJdbcConnection(), sql, handler, params);
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
    public Object findSingleByRawSql(String sql) {
        throw new NotImplementedException("need a bean class");
    }

    @Override
    public IWrappedQuery createQuery(Class<?> cls, String queryString) {
        // FIXME
        try {
            return new JdbcQuery(queryString, new NamedParameterStatement(getJdbcConnection(), queryString), getEntityMetaOfClass(cls));
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public IWrappedQuery createQuery(String queryString) {
        // FIXME
        try {
            return new JdbcQuery(queryString, new NamedParameterStatement(getJdbcConnection(), queryString), null);
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public int executeNativeSql(String sql, ParameterBindings parameterBindings) {
        PreparedStatement pstm = prepareStatement(sql);
        if (parameterBindings != null) {
            parameterBindings.applyToPrepareStatement(pstm);
        }
        return executePrepareStatementUpdate(pstm);
    }

    @Override
    public int executeQuerySql(String sql, ParameterBindings parameterBindings) {
        return executeNativeSql(sql, parameterBindings);
    }

    @Override
    public String columnNameInQuery(Class<?> modelCls, String propertyName) {
        ModelMeta modelMeta = getEntityMetaOfClass(modelCls);
        ModelMeta.ModelColumnMeta modelColumnMeta = modelMeta.getColumnMetaByFieldName(propertyName);
        return modelColumnMeta != null ? modelColumnMeta.columnName : propertyName;
    }
}
