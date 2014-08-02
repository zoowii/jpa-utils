package com.zoowii.jpa_utils.query;

import com.zoowii.jpa_utils.util.ListUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Expr {
    public static final String EQ = "=";
    public static final String NE = "!=";
    public static final String LT = "<";
    public static final String LE = "<=";
    public static final String GT = ">";
    public static final String GE = ">=";
    public static final String OR = "or";
    public static final String AND = "and";
    public static final String LIKE = "like";
    protected String op = null;
    protected List<Object> items = new ArrayList<Object>();

    public Expr() {
    }

    public Expr(String op, List<Object> items) {
        this.op = op;
        this.items = items;
    }

    public static class EmptyExpr extends Expr {
        @Override
        public Map<String, Object> toQueryString(Query query) {
            return ListUtil.hashmap("query",
                    "1=1", "bindings", new ParameterBindings());
        }
    }

    public static Expr dummy() {
        return new EmptyExpr();
    }

    public static Expr createEQ(String name, Object value) {
        return new Expr(EQ, ListUtil.seq(name, value));
    }

    public Expr eq(String name, Object value) {
        return createAND(this, createEQ(name, value));
    }

    public static Expr createNE(String name, Object value) {
        return new Expr(NE, ListUtil.seq(name, value));
    }

    public Expr ne(String name, Object value) {
        return createAND(this, createNE(name, value));
    }

    public static Expr createLT(String name, Object value) {
        return new Expr(LT, ListUtil.seq(name, value));
    }

    public Expr lt(String name, Object value) {
        return createAND(this, createLT(name, value));
    }

    public static Expr createLE(String name, Object value) {
        return new Expr(LE, ListUtil.seq(name, value));
    }

    public Expr le(String name, Object value) {
        return createAND(this, createLE(name, value));
    }

    public static Expr createGT(String name, Object value) {
        return new Expr(GT, ListUtil.seq(name, value));
    }

    public Expr gt(String name, Object value) {
        return createAND(this, createGT(name, value));
    }

    public static Expr createGE(String name, Object value) {
        return new Expr(GE, ListUtil.seq(name, value));
    }

    public Expr like(String name, Object value) {
        return createLIKE(name, value);
    }

    public static Expr createLIKE(String name, Object value) {
        return new Expr(LIKE, ListUtil.seq(name, value));
    }

    public Expr ge(String name, Object value) {
        return createAND(this, createGE(name, value));
    }

    public static Expr createOR(Expr left, Expr right) {
        return new OrExpr(OR, ListUtil.seq((Object) left, right));
    }

    public Expr or(Expr other) {
        return createOR(this, other);
    }

    public static Expr createAND(Expr left, Expr right) {
        return new AndExpr(AND, ListUtil.seq((Object) left, right));
    }

    public Expr and(Expr other) {
        return createAND(this, other);
    }

    /**
     * parse to Query string and bindings
     */
    public Map<String, Object> toQueryString(Query query) {
        String queryStr = items.get(0) + " " + op + " ?";
//        List<Object> bindings = ListUtil.seq((Object) items.get(1));
        ParameterBindings bindings = new ParameterBindings();
        bindings.addIndexBinding(items.get(1));
        return ListUtil.hashmap("query", queryStr, "bindings", bindings);
    }
}
