package com.zoowii.jpa_utils.query;

import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
import com.zoowii.jpa_utils.util.ListUtil;

import java.util.List;
import java.util.Map;

public class OrExpr extends Expr {
    public OrExpr() {
    }

    public OrExpr(String op, List<Object> items) {
        super(op, items);
    }

    @Override
    public QueryInfo toQueryString(SqlMapper sqlMapper, Query query) {
        Expr left = (Expr) items.get(0);
        Expr right = (Expr) items.get(1);
        QueryInfo leftQuery = left.toQueryString(sqlMapper, query);
        QueryInfo rightQuery = right.toQueryString(sqlMapper, query);
        String queryStr = "(" + leftQuery.getQueryString() + " " + op + " " + rightQuery.getQueryString() + ")";
        ParameterBindings bindings = new ParameterBindings();
        bindings = bindings.addAll(leftQuery.getParameterBindings());
        bindings = bindings.addAll(rightQuery.getParameterBindings());
        return new QueryInfo(queryStr, bindings);
    }
}
