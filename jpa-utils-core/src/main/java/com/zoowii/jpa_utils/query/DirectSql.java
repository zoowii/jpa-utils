package com.zoowii.jpa_utils.query;

/**
 * direct query sql string
 * 在.gt(prop, Query.sql(...))等查询中,作为非parameter参数,而是直接并入到query sql中
 * Created by zoowii on 15/11/14.
 */
public class DirectSql {
    private String sql = "";

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public DirectSql(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return sql;
    }
}
