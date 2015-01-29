package com.zoowii.jpa_utils.jdbcorm.sqlmapper;

import com.google.common.base.Function;
import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.core.impl.JdbcQuery;
import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.jdbcorm.NamedParameterStatement;
import com.zoowii.jpa_utils.jdbcorm.SqlStatementInfo;
import com.zoowii.jpa_utils.query.ParameterBindings;
import com.zoowii.jpa_utils.util.FieldAccessor;
import com.zoowii.jpa_utils.util.IncrementCircleNumber;
import com.zoowii.jpa_utils.util.ListUtil;
import com.zoowii.jpa_utils.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zoowii on 15/1/26.
 */
public class MySQLMapper extends SqlMapper {

    @Override
    public String getOfInteger() {
        return "int(11)";
    }

    @Override
    public String getOfLong() {
        return "bigint";
    }

    @Override
    public String getOfString() {
        return "varchar(255)";
    }

    @Override
    public String getOfBoolean() {
        return "int(11)";
    }

    @Override
    public String getOfString(int length) {
        return String.format("varchar(%d)", length);
    }

    @Override
    public String getOfText(boolean isLob) {
        return isLob ? "text" : "varchar(2000)";
    }

    @Override
    public String getOfBytes(boolean isLob) {
        return "blob";
    }

    @Override
    public String getOfDate() {
        return "date";
    }

    @Override
    public String getOfDateTime() {
        return "datetime";
    }

    @Override
    public String getOfTimestamp() {
        return "timestamp";
    }

    private String generateTableAlias(ModelMeta modelMeta) {
        return modelMeta.getTableName() + "_" + incrementCircleNumber.getAndIncrement();
    }

    @Override
    public String getSqlColumnNameWrapped(String columnName) {
        return String.format("`%s`", columnName);
    }

    @Override
    public String getSqlTableNameWrapped(String tableName) {
        return String.format("`%s`", tableName);
    }

    @Override
    public IWrappedQuery limit(IWrappedQuery query, ParameterBindings parameterBindings, int limit) {
        assert query instanceof JdbcQuery;
        JdbcQuery jdbcQuery = (JdbcQuery) query;
        String var = "limit_" + incrementCircleNumber.getAndIncrement();
        String sql = String.format(" %s LIMIT :%s ", jdbcQuery.getSql(), var);
        try {
            jdbcQuery = new JdbcQuery(sql, jdbcQuery.getNamedParameterStatement().clonePure(sql), jdbcQuery.getModelMeta());
            parameterBindings.addBinding(var, limit);
            return jdbcQuery;
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }

    @Override
    public IWrappedQuery offset(IWrappedQuery query, ParameterBindings parameterBindings, int offset) {
        assert query instanceof JdbcQuery;
        JdbcQuery jdbcQuery = (JdbcQuery) query;
        String var = "offset_" + incrementCircleNumber.getAndIncrement();
        String sql = String.format(" %s OFFSET :%s ", jdbcQuery.getSql(), var);
        try {
            jdbcQuery = new JdbcQuery(sql, jdbcQuery.getNamedParameterStatement().clonePure(sql), jdbcQuery.getModelMeta());
            parameterBindings.addBinding(var, offset);
            return jdbcQuery;
        } catch (SQLException e) {
            throw new JdbcRuntimeException(e);
        }
    }
}
