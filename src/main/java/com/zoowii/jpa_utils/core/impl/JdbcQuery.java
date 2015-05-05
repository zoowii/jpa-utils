package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.core.IWrappedTypedQuery;
import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.jdbcorm.NamedParameterStatement;
import com.zoowii.jpa_utils.query.ParameterBindings;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by zoowii on 15/1/29.
 */
public class JdbcQuery implements IWrappedTypedQuery {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcQuery.class);

    private final NamedParameterStatement namedParameterStatement;
    private final String sql;
    private final ModelMeta modelMeta;

    public NamedParameterStatement getNamedParameterStatement() {
        return namedParameterStatement;
    }

    public String getSql() {
        return sql;
    }

    public ModelMeta getModelMeta() {
        return modelMeta;
    }

    public JdbcQuery(String sql, NamedParameterStatement namedParameterStatement, ModelMeta modelMeta) {
        this.namedParameterStatement = namedParameterStatement;
        this.sql = sql;
        this.modelMeta = modelMeta;
    }

    @Override
    public IWrappedQuery setParameter(int index, Object value) {
        try {
            namedParameterStatement.getStatement().setObject(index, value);
            return new JdbcQuery(sql, namedParameterStatement, modelMeta);
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public IWrappedQuery setParameter(String key, Object value) {
        try {
            namedParameterStatement.setObject(key, value);
            return new JdbcQuery(sql, namedParameterStatement, modelMeta);
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public IWrappedQuery setMaxResults(int limit) {
        // TODO: may need change the sql
        try {
            namedParameterStatement.getStatement().setMaxRows(limit);
            return new JdbcQuery(sql, namedParameterStatement, modelMeta);
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public IWrappedQuery setMaxResults(Session session, ParameterBindings parameterBindings, int limit) {
//        try {
//            namedParameterStatement.getStatement().setMaxRows(limit);
//            return new JdbcQuery(sql, namedParameterStatement, modelMeta);
//        } catch (SQLException e) {
//            throw new JdbcRuntimeException(e);
//        }
        return session.getSqlMapper().limit(this, parameterBindings, limit);
    }

    @Override
    public IWrappedQuery setFirstResult(int offset) {
        return this;
    }

    @Override
    public IWrappedQuery setFirstResult(Session session, ParameterBindings parameterBindings, int offset) {
        return session.getSqlMapper().offset(this, parameterBindings, offset);
    }

    @Override
    public List getResultList() {
        try {
            ResultSetHandler<List<Object>> handler = JdbcSession.getListResultSetHandler(modelMeta);
            LOG.info("query sql: " + namedParameterStatement.getQuery());
            ResultSet resultSet = namedParameterStatement.executeQuery();
            try {
                return handler.handle(resultSet);
            } finally {
                resultSet.close();
            }
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public Object getSingleResult() {
        try {
            ResultSetHandler<Object> handler = JdbcSession.getRowBeanResultSetHandler(modelMeta);
            ResultSet resultSet = namedParameterStatement.executeQuery();
            try {
                return handler.handle(resultSet);
            } finally {
                resultSet.close();
            }
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public int executeUpdate() {
        try {
            return namedParameterStatement.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }
}
