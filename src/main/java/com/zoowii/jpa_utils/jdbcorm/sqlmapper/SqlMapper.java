package com.zoowii.jpa_utils.jdbcorm.sqlmapper;

import com.google.common.base.Function;
import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.jdbcorm.SqlStatementInfo;
import com.zoowii.jpa_utils.query.Expr;
import com.zoowii.jpa_utils.query.ParameterBindings;
import com.zoowii.jpa_utils.util.FieldAccessor;
import com.zoowii.jpa_utils.util.IncrementCircleNumber;
import com.zoowii.jpa_utils.util.ListUtil;
import com.zoowii.jpa_utils.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * map java orm model's property types to sql column types
 * Created by zoowii on 15/1/26.
 */
public abstract class SqlMapper {
    protected IncrementCircleNumber incrementCircleNumber = new IncrementCircleNumber(0, Long.MAX_VALUE);

    public abstract String getOfInteger();

    public abstract String getOfLong();

    public abstract String getOfFloat();

    public abstract String getOfDouble();

    public abstract String getOfString();

    public abstract String getOfBoolean();

    public abstract String getOfString(int length);

    public abstract String getOfText(boolean isLob);

    public abstract String getOfBytes(boolean isLob);

    public abstract String getOfDate();

    public abstract String getOfDateTime();

    public abstract String getOfTimestamp();

    public String get(Class<?> propertyCls, javax.persistence.Column columnAnno, boolean isLob) {
        if (columnAnno != null && !StringUtil.isEmpty(columnAnno.columnDefinition())) {
            return columnAnno.columnDefinition();
        }
        if (propertyCls == Integer.class
                || "int".equals(propertyCls.getName())) {
            return getOfInteger();
        }
        if (propertyCls == Long.class
                || "long".equals(propertyCls.getName())) {
            return getOfLong();
        }
        if(propertyCls == Float.class
                || "float".equals(propertyCls.getName())) {
            return getOfFloat();
        }
        if(propertyCls == Double.class
                || "double".equals(propertyCls.getName())) {
            return getOfDouble();
        }
        if (propertyCls == String.class) {
            if (isLob) {
                return getOfText(true);
            }
            if (columnAnno != null) {
                return getOfString(columnAnno.length());
            } else {
                return getOfString();
            }
        }
        if (propertyCls == Boolean.class
                || "boolean".equals(propertyCls.toString())) {
            return getOfBoolean();
        }
        if (propertyCls == Date.class || propertyCls == java.sql.Time.class) {
            return getOfDateTime();
        }
        if (propertyCls == java.sql.Date.class) {
            return getOfDate();
        }
        if (propertyCls == java.sql.Timestamp.class
                || propertyCls == java.sql.Time.class) {
            return getOfTimestamp();
        }
        if (propertyCls.isArray() && (propertyCls.getComponentType() == Byte.class || propertyCls.getComponentType().getName().equals("byte"))) {
            return getOfBytes(isLob);
        }
        throw new JdbcRuntimeException("Can't find sql column type of column type " + propertyCls.getName());
    }

    /**
     *  wrap the column name to avoid distinct with sql key words, eg. `columnName` in MySQL
     *
     * @param columnName column name to wrap
     * @return wrapped column name in sql
     */
    public String getSqlColumnNameWrapped(String columnName) {
        return columnName;
    }

    /**
     * wrap the table name to avoid distinct with sql key words, eg. `tableName` in MySQL
     *
     * @param tableName table name
     * @return wrapped table name
     */
    public String getSqlTableNameWrapped(String tableName) {
        return tableName;
    }

    public Pair<String, String> getFromSubSql(ModelMeta modelMeta, boolean useAlias) {
        String tableFullName = StringUtil.isEmpty(
                modelMeta.getTableSchema()) ? getSqlTableNameWrapped(modelMeta.getTableName()) : String.format("%s.%s", modelMeta.getTableSchema(), getSqlTableNameWrapped(modelMeta.getTableName()));
        String tableAlias = useAlias ? String.format("%s_%s", modelMeta.getTableName(), incrementCircleNumber.getAndIncrement() + "") : null;
        String fromSql = useAlias ? String.format(" FROM %s %s ", tableFullName, tableAlias) : String.format(" FROM %s ", tableFullName);
        return Pair.of(fromSql, tableAlias);
    }

    public String getColumnsSql(ModelMeta modelMeta, final String tableAlias, boolean includeId) {
        List<String> columnSqlNames = new ArrayList<String>();
        for (ModelMeta.ModelColumnMeta columnMeta : modelMeta.getColumnMetaSet()) {
            if (columnMeta.isId && !includeId) {
                continue;
            }
            String columnSql = getSqlColumnNameWrapped(columnMeta.columnName);
            columnSqlNames.add(columnSql);
        }
        return StringUtil.join(ListUtil.map(columnSqlNames, new Function<String, String>() {
            @Override
            public String apply(String columnSql) {
                return tableAlias != null ? (tableAlias + "." + columnSql) : columnSql;
            }
        }), ",");
    }

    public SqlStatementInfo getUpdate(final ModelMeta modelMeta, final Object entity, Function<ParameterBindings, String> conditionSqlGenerator) {
        final ParameterBindings parameterBindings = new ParameterBindings();
        String tableFullName = StringUtil.isEmpty(
                modelMeta.getTableSchema()) ? getSqlTableNameWrapped(modelMeta.getTableName()) : String.format("%s.%s", modelMeta.getTableSchema(), getSqlTableNameWrapped(modelMeta.getTableName()));
        List<Pair<String, String>> columnNameAndSqlNames = new ArrayList<Pair<String, String>>(); // left is field name like 'name', and right is name in sql like '{tableAlias}.`name`'
        for (ModelMeta.ModelColumnMeta columnMeta : modelMeta.getColumnMetaSet()) {
            if (columnMeta.isId) {
                continue;
            }
            String columnSql = getSqlColumnNameWrapped(columnMeta.columnName);
            columnNameAndSqlNames.add(Pair.of(columnMeta.fieldName, columnSql));
        }
        final List<String> columnSetPairs = new ArrayList<String>();
        ListUtil.map(columnNameAndSqlNames, new Function<Pair<String, String>, Object>() {
            @Override
            public Object apply(Pair<String, String> pair) {
                FieldAccessor fieldAccessor = new FieldAccessor(modelMeta.getModelCls(), pair.getLeft());
                String key = String.format("%s%s", pair.getLeft(), incrementCircleNumber.getAndIncrement() + "");
                parameterBindings.addBinding(key, fieldAccessor.getProperty(entity));
                columnSetPairs.add(String.format("%s = :%s", pair.getRight(), key));
                return null;
            }
        });
        String setItemsSql = StringUtil.join(columnSetPairs, ",");
        String condition;
        if (conditionSqlGenerator != null) {
            condition = conditionSqlGenerator.apply(parameterBindings);
        } else {
            condition = " 1 = 1 ";
        }
        String sql = String.format("UPDATE %s SET %s %s ", tableFullName, setItemsSql, getWhereSubSql(condition));
        return SqlStatementInfo.of(sql, parameterBindings);
    }

    public SqlStatementInfo getInsert(ModelMeta modelMeta, Object entity) {
        String tableFullName = StringUtil.isEmpty(
                modelMeta.getTableSchema()) ? getSqlTableNameWrapped(modelMeta.getTableName()) : String.format("%s.%s", modelMeta.getTableSchema(), getSqlTableNameWrapped(modelMeta.getTableName()));
        List<Pair<String, String>> columnNameAndSqlNames = new ArrayList<Pair<String, String>>(); // left is field name like 'name', and right is name in sql like '{tableAlias}.`name`'
        boolean includeId = true;
        for (ModelMeta.ModelColumnMeta columnMeta : modelMeta.getColumnMetaSet()) {
            if (columnMeta.isId) {
                FieldAccessor fieldAccessor = new FieldAccessor(modelMeta.getModelCls(), columnMeta.fieldName);
                Object idValue = fieldAccessor.getProperty(entity);
                if (idValue == null) {
                    // TODO: use generator strategy to generate new id value
                    includeId = false;
                    continue;
                }
            }
            String columnSql = getSqlColumnNameWrapped(columnMeta.columnName);
            columnNameAndSqlNames.add(Pair.of(columnMeta.fieldName, columnSql));
        }
        ParameterBindings parameterBindings = new ParameterBindings();
        for (Pair<String, String> columnNameAndSqlName : columnNameAndSqlNames) {
            String fieldName = columnNameAndSqlName.getLeft();
            FieldAccessor fieldAccessor = new FieldAccessor(modelMeta.getModelCls(), fieldName);
//            parameterBindings.addIndexBinding(fieldAccessor.getProperty(entity));
            parameterBindings.addBinding(fieldName, fieldAccessor.getProperty(entity));
        }
        String columnsSql = getColumnsSql(modelMeta, null, includeId);
        String valuesBindingSql = StringUtil.join(ListUtil.map(columnNameAndSqlNames, new Function<Pair<String, String>, String>() {
            @Override
            public String apply(Pair<String, String> pair) {
                return String.format(":%s", pair.getLeft());
            }
        }), ",");
        String sql = String.format("INSERT INTO %s ( %s ) VALUES ( %s )", tableFullName, columnsSql, valuesBindingSql);
        return SqlStatementInfo.of(sql, parameterBindings);
    }

    public String getWhereSubSql(String whereConditions) {
        return " WHERE " + whereConditions;
    }

    public String getOpConditionSubSql(String op, ModelMeta modelMeta, Object left, Object value, ParameterBindings parameterBindings, String tableAlias) {
        ModelMeta.ModelColumnMeta columnMeta = modelMeta.getColumnMetaByFieldName(left.toString());
        if (columnMeta == null) {
            String key = "var_" + incrementCircleNumber.getAndIncrement();
            parameterBindings.addBinding(key, value);
            return String.format(" (%s %s :%s)", left, op, key);
        }
        String columnName = columnMeta.columnName;
        String finalTable = tableAlias != null ? (tableAlias + "." + getSqlColumnNameWrapped(columnName)) : getSqlColumnNameWrapped(columnName);
        if(Expr.IN.equals(op)) {
            if(value instanceof List) {
                List<Object> valueList = (List<Object>) value;
                for(int i=0;i<valueList.size();++i) {
                    if(valueList.get(i) instanceof String) {
                        valueList.set(i, String.format("'%s'", valueList.get(i)));
                    }
                }
                value = StringUtil.join((List<?>) value, ", ");
            }
            return String.format(" (%s %s (%s)) ", finalTable, op, value);
        }
        String key = left.toString() + incrementCircleNumber.getAndIncrement();
        parameterBindings.addBinding(key, value);
        return String.format(" (%s %s :%s) ", finalTable, op, key);
    }

    public String getEqConditionSubSql(ModelMeta modelMeta, Object fieldName, Object value, ParameterBindings parameterBindings, String tableAlias) {
        return getOpConditionSubSql("=", modelMeta, fieldName, value, parameterBindings, tableAlias);
    }

    public String getIdEqConditionSubSql(ModelMeta modelMeta, Object value, ParameterBindings parameterBindings, String tableAlias) {
        String idFieldName = modelMeta.getIdColumnMeta().fieldName;
        return getEqConditionSubSql(modelMeta, idFieldName, value, parameterBindings, tableAlias);
    }

    public String getSelectSubSql(String selectContent) {
        return String.format(" SELECT %S ", selectContent);
    }

    public String getDeleteSubSql(String fromSql, String whereSql) {
        return String.format("DELETE %s %s", fromSql, whereSql != null ? whereSql : "");
    }

    public String getOrderBySubSql(String orderBy) {
        return String.format(" ORDER BY %s ", orderBy);
    }

    /**
     * create a placeholder like ':abc', '?' in sql of HQL or preparestatement
     *
     * @param parameterBindings parameter bindings
     * @param key parameter key
     * @param value parameter value
     * @return pair of {left: varName,比如abc, right: placeholder,eg. abc}
     */
    public Pair<String, String> getNewParameterVar(ParameterBindings parameterBindings, String key, Object value) {
        String var = String.format("%s_%s", key, incrementCircleNumber.getAndIncrement() + "");
        parameterBindings.addBinding(var, value);
        return Pair.of(var, ":" + var);
    }

    /**
     * the generated query may now not contains select sub query, eg. in HQL.
     *
     * @param modelMeta model meta info
     * @param sql sql to wrap
     * @return wrapped query string
     */
    public String wrapQueryWithDefaultSelect(ModelMeta modelMeta, String sql) {
        if (sql.trim().toUpperCase().startsWith("FROM".toUpperCase())) {
            return " SELECT * " + sql;
        } else {
            return sql;
        }
    }

    public abstract IWrappedQuery limit(IWrappedQuery query, ParameterBindings parameterBindings, int limit);

    public abstract IWrappedQuery offset(IWrappedQuery query, ParameterBindings parameterBindings, int offset);
}
