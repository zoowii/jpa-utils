package com.zoowii.jpa_utils.jdbcorm.sqlmapper;

/**
 * TODO
 * Created by zoowii on 2015/7/19.
 */
public class PgSQLMapper extends MySQLMapper {
    @Override
    public String getSqlColumnNameWrapped(String columnName) {
        return String.format("%s", columnName);
    }

    @Override
    public String getSqlTableNameWrapped(String tableName) {
        return String.format("%s", tableName);
    }
}
