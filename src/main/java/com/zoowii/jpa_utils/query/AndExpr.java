package com.zoowii.jpa_utils.query;

import com.zoowii.jpa_utils.util.ListUtil;

import java.util.List;
import java.util.Map;

public class AndExpr extends Expr {
    public AndExpr() {
    }

    public AndExpr(String op, List<Object> items) {
        super(op, items);
    }

    @Override
    public Map<String, Object> toQueryString(Query query) {
        Expr left = (Expr) items.get(0);
        Expr right = (Expr) items.get(1);
        Map<String, Object> leftQuery = left.toQueryString(query);
        Map<String, Object> rightQuery = right.toQueryString(query);
        String queryStr = "(" + leftQuery.get("query") + " " + op + " " + rightQuery.get("query") + ")";
        ParameterBindings bindings = new ParameterBindings();
        bindings = bindings.addAll((ParameterBindings) leftQuery.get("bindings"));
        bindings = bindings.addAll((ParameterBindings) rightQuery.get("bindings"));
        return ListUtil.hashmap("query", queryStr, "bindings", bindings);
    }
}
