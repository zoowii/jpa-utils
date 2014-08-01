package com.zoowii.hibernate_utils.query;

import com.zoowii.hibernate_utils.util.ListUtil;

import java.util.List;
import java.util.Map;

public class OrExpr extends Expr {
    public OrExpr() {
    }

    public OrExpr(String op, List<Object> items) {
        super(op, items);
    }

    @Override
    public Map<String, Object> toQueryString(Query query) {
        Expr left = (Expr) items.get(0);
        Expr right = (Expr) items.get(1);
        Map<String, Object> leftQuery = left.toQueryString(query);
        Map<String, Object> rightQuery = right.toQueryString(query);
        String queryStr = "(" + leftQuery.get("query") + " " + op + " " + rightQuery.get("query") + ")";
        List<Object> bindings = ListUtil.seq();
        bindings.addAll((List<Object>) leftQuery.get("bindings"));
        bindings.addAll((List<Object>) rightQuery.get("bindings"));
        return ListUtil.hashmap("query", queryStr, "bindings", bindings);
    }
}
