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
 * TODO: support methods like MyBatis
 * Created by zoowii on 15/1/26.
 */
public class JdbcSession extends AbstractSession {
    private static final Logger LOG = Logger.getLogger(JdbcSession.class);

    private java.sql.Connection jdbcConnection;
    private JdbcSessionFactory jdbcSessionFactory;
    private AtomicBoolean activeFlag = new AtomicBoolean(false);
    private SqlMapper sqlMapper = new MySQLMapper(); // FIXME

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
     * TODO: may add feature like @Insert("... ${column}..."),@Update, @Delete, @Query to define query
     *
     * @param entity entity to save
     */
    @Override
    public void save(Object entity) {
        try {
            ModelMeta modelMeta = getEntityMetaOfClass(entity.getClass());
            SqlStatementInfo insertSqlInfo = modelMeta.getSqlMapper().getInsert(modelMeta, entity);
            LOG.info("insert sql: " + insertSqlInfo.getSql());
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
            NamedParameterStatement namedParameterStatement = new NamedParameterStatement(getJdbcConnection(), updateSqlInfo.getSql());
            try {
                updateSqlInfo.getParameterBindings().applyToNamedPrepareStatement(namedParameterStatement);
                namedParameterStatement.executeUpdate();
            } finally {
                namedParameterStatement.close();
            }
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public void merge(Object entity) {

    }
    
    @Override
    public void clear() {

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

    public static ResultSetHandler<List<Object>> getListResultSetHandler(Class<?> cls) {
        return new BeanListHandler(cls); // TODO: use modelMeta to define row processor
    }

    public static ResultSetHandler<Object> getRowBeanResultSetHandler(Class<?> cls) {
        return new BeanHandler(cls); // TODO: use modelMeta to define row processor
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
            LOG.info("query sql " + sql);
//            QueryRunner runner = new QueryRunner();
            ResultSetHandler<List<Object>> handler = getListResultSetHandler(cls);
            NamedParameterStatement namedParameterStatement = new NamedParameterStatement(getJdbcConnection(), sql);
            try {
                parameterBindings.applyToNamedPrepareStatement(namedParameterStatement);
                ResultSet resultSet = namedParameterStatement.executeQuery();
                try {
                    List<Object> result = handler.handle(resultSet);
//            List<Object> result = runner.query(getJdbcConnection(), sql, handler, parameterBindings.getIndexParametersArray());
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
            LOG.info("delete sql: " + sql);
            NamedParameterStatement namedParameterStatement = new NamedParameterStatement(getJdbcConnection(), sql);
            try {
                parameterBindings.applyToNamedPrepareStatement(namedParameterStatement);
                namedParameterStatement.executeUpdate();
            } finally {
                namedParameterStatement.close();
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

    public int executeNativeSql(String sql, ParameterBindings parameterBindings) {
        PreparedStatement pstm = prepareStatement(sql);
        if (parameterBindings != null) {
            parameterBindings.applyToPrepareStatement(pstm);
        }
        return executePrepareStatementUpdate(pstm);
    }
}
