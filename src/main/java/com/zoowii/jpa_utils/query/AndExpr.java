package com.zoowii.jpa_utils.query;

import java.util.List;

public class AndExpr extends Expr {
    public AndExpr() {
    }

    public AndExpr(String op, List<Object> items) {
        super(op, items);
    }

    @Override
    public QueryInfo toQueryString(Query query) {
        Expr left = (Expr) items.get(0);
        Expr right = (Expr) items.get(1);
        QueryInfo leftQuery = left.toQueryString(query);
        QueryInfo rightQuery = right.toQueryString(query);
        String queryStr = "(" + leftQuery.getQueryString() + " " + op + " " + rightQuery.getQueryString() + ")";
        ParameterBindings bindings = new ParameterBindings();
        bindings = bindings.addAll(leftQuery.getParameterBindings());
        bindings = bindings.addAll(rightQuery.getParameterBindings());
        return new QueryInfo(queryStr, bindings);
    }
}
