package com.zoowii.jpa_utils.query;

import com.zoowii.jpa_utils.jdbcorm.sqlmapper.ORMSqlMapper;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
import com.zoowii.jpa_utils.util.ListUtil;
import org.apache.commons.lang3.NotImplementedException;

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
    public static final String IN = "in";
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
        public QueryInfo toQueryString(SqlMapper sqlMapper, Query query) {
            return new QueryInfo("1=1", new ParameterBindings());
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

    public static Expr createIN(String property, Object value) {
        return new Expr(IN, ListUtil.seq(property, value));
    }

    public Expr and(Expr other) {
        return createAND(this, other);
    }

    public Expr in(String property, Object value) {
        return createAND(this, createIN(property, value));
    }

    /**
     * whether this expr is `is null` or `is not null` sub sql
     *
     * @return
     */
    private boolean isNullOrNotNullExpr() {
        if (op.equals(EQ) || op.equals(NE)) {
            return items.size() == 2 && items.get(1) == null;
        } else {
            return false;
        }
    }

    private static final List<String> NEED_PARSE_LEFT_COLUMN_OPS = ListUtil.seq(EQ,NE, GT, LT, LIKE, GE, LE, IN);

    /**
     * parse to Query string and bindings
     * @param sqlMapper sql mapper to use
     * @param query query instance
     * @return query info 
     */
    public QueryInfo toQueryString(SqlMapper sqlMapper, Query query) {
        if (isNullOrNotNullExpr()) {
            Object item1 = items.get(0);
            if(NEED_PARSE_LEFT_COLUMN_OPS.contains(op)) {
                if(item1 instanceof String) {
                    item1 = query.getSession().columnNameInQuery(query.getModelClass(), (String) item1);
                }
            }
            String queryStr = item1 + " " + (op.equals(EQ) ? "is null" : "is not null");
            return new QueryInfo(queryStr, new ParameterBindings());
        }
        ParameterBindings bindings = new ParameterBindings();
        String queryStr = sqlMapper.getOpConditionSubSql(op, query.getSession().getEntityMetaOfClass(query.cls), items.get(0), items.get(1), bindings, null);
//        String placement = sqlMapper.getNewParameterVar(bindings, "var", items.get(1)).getRight();
//        String queryStr = items.get(0) + " " + op + " " + placement;
        return new QueryInfo(queryStr, bindings);
    }

    public QueryInfo toQueryString(SqlMapper sqlMapper) {
//        if (isNullOrNotNullExpr()) {
//            String queryStr = items.get(0) + " " + (op.equals(EQ) ? "is null" : "is not null");
//            return new QueryInfo(queryStr, new ParameterBindings());
//        }
//        sqlMapper.getOpConditionSubSql(op, )
//        ParameterBindings bindings = new ParameterBindings();
//        String placement = sqlMapper.getNewParameterVar(bindings, "var", items.get(1)).getRight();
//        String queryStr = items.get(0) + " " + op + " " + placement;
//        return new QueryInfo(queryStr, bindings);
        throw new NotImplementedException("need query object");
    }
}
