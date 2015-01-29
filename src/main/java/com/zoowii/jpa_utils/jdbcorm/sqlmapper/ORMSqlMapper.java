package com.zoowii.jpa_utils.jdbcorm.sqlmapper;

import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.query.ParameterBindings;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by zoowii on 15/1/29.
 */
public class ORMSqlMapper extends SqlMapper {
    @Override
    public String getOfInteger() {
        return "int";
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
        return "int";
    }

    @Override
    public String getOfString(int length) {
        return String.format("varchar(%d)", length);
    }

    @Override
    public String getOfText(boolean isLob) {
        return "text";
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

    @Override
    public Pair<String, String> getFromSubSql(ModelMeta modelMeta, boolean useAlias) {
        String tableAlias = useAlias ? String.format("%s_%s", modelMeta.getModelCls().getSimpleName(), incrementCircleNumber.getAndIncrement() + "") : null;
        String fromSql = useAlias ? String.format(" FROM %s %s ", modelMeta.getModelCls().getSimpleName(), tableAlias) : String.format(" FROM %s ", modelMeta.getModelCls().getSimpleName());
        return Pair.of(fromSql, tableAlias);
    }

    @Override
    public Pair<String, String> getNewParameterVar(ParameterBindings parameterBindings, String key, Object value) {
        String var = "?";
        parameterBindings.addIndexBinding(value);
        return Pair.of(key, var);
    }

    @Override
    public String wrapQueryWithDefaultSelect(ModelMeta modelMeta, String sql) {
        return sql;
    }

    @Override
    public IWrappedQuery limit(IWrappedQuery query, ParameterBindings parameterBindings, int limit) {
        return query;
    }

    @Override
    public IWrappedQuery offset(IWrappedQuery query, ParameterBindings parameterBindings, int offset) {
        return query;
    }

    @Override
    public String getOpConditionSubSql(String op, ModelMeta modelMeta, Object fieldName, Object value, ParameterBindings parameterBindings, String tableAlias) {
        parameterBindings.addIndexBinding(value);
        return String.format(" (%s %s ?) ", fieldName, op);
    }
}
