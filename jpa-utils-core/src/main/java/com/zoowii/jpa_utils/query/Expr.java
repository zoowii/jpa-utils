package com.zoowii.jpa_utils.query;

import com.zoowii.jpa_utils.enums.SqlTypes;
import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
import com.zoowii.jpa_utils.util.ListUtil;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

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
    public static final String EXISTS = "exists";
    public static final String NOT = "not";
    protected String op = null;
    protected List<Object> items = new ArrayList<Object>();

    public static final String JSONB_CAST_TMPL = "CAST(%s AS JSONB)";

    /**
     * the operators in the sql may need wrapped like CAST(? AS JSONB), the wrapper is String.format template, eg. CAST(%s AS JSONB)
     */
    private String op1Wrapper = null;
    private String op2Wrapper = null;

    public Expr() {
    }

    public Expr(String op, List<Object> items) {
        this.op = op;
        this.items = items;
    }

    public Expr setOp1Wrapper(String wrapper) {
        this.op1Wrapper = wrapper;
        return this;
    }

    public Expr setOp2Wrapper(String wrapper) {
        this.op2Wrapper = wrapper;
        return this;
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
        return createEQ(name, value, SqlTypes.ANY);
    }

    public static Expr createEQ(String name, Object value, int sqlType) {
        return new Expr(EQ, ListUtil.seq(name, value)).setOp2Wrapper(sqlType == SqlTypes.JSONB ? JSONB_CAST_TMPL : null);
    }

    public Expr eq(String name, Object value) {
        return eq(name, value, SqlTypes.ANY);
    }

    public Expr eq(String name, Object value, int sqlType) {
        return createAND(this, createEQ(name, value, sqlType));
    }

    public static Expr createNE(String name, Object value) {
        return createNE(name, value, SqlTypes.ANY);
    }

    public static Expr createNE(String name, Object value, int sqlType) {
        return new Expr(NE, ListUtil.seq(name, value)).setOp2Wrapper(sqlType == SqlTypes.JSONB ? JSONB_CAST_TMPL : null);
    }

    public Expr ne(String name, Object value) {
        return ne(name, value, SqlTypes.ANY);
    }

    public Expr ne(String name, Object value, int sqlType) {
        return createAND(this, createNE(name, value, sqlType));
    }

    public static Expr createLT(String name, Object value) {
        return createLT(name, value, SqlTypes.ANY);
    }

    public static Expr createLT(String name, Object value, int sqlType) {
        return new Expr(LT, ListUtil.seq(name, value)).setOp2Wrapper(sqlType == SqlTypes.JSONB ? JSONB_CAST_TMPL : null);
    }

    public Expr lt(String name, Object value) {
        return lt(name, value, SqlTypes.ANY);
    }

    public Expr lt(String name, Object value, int sqlType) {
        return createAND(this, createLT(name, value, sqlType));
    }

    public static Expr createLE(String name, Object value) {
        return createLE(name, value, SqlTypes.ANY);
    }

    public static Expr createLE(String name, Object value, int sqlType) {
        return new Expr(LE, ListUtil.seq(name, value)).setOp2Wrapper(sqlType == SqlTypes.JSONB ? JSONB_CAST_TMPL : null);
    }
    public Expr le(String name, Object value) {
        return le(name, value, SqlTypes.ANY);
    }

    public Expr le(String name, Object value, int sqlType) {
        return createAND(this, createLE(name, value, sqlType));
    }

    public static Expr createGT(String name, Object value) {
        return createGT(name, value, SqlTypes.ANY);
    }

    public static Expr createGT(String name, Object value, int sqlType) {
        return new Expr(GT, ListUtil.seq(name, value)).setOp2Wrapper(sqlType == SqlTypes.JSONB ? JSONB_CAST_TMPL : null);
    }

    public Expr gt(String name, Object value) {
        return gt(name, value, SqlTypes.ANY);
    }

    public Expr gt(String name, Object value, int sqlType) {
        return createAND(this, createGT(name, value, sqlType));
    }
    public static Expr createGE(String name, Object value) {
        return createGE(name, value, SqlTypes.ANY);
    }

    public static Expr createGE(String name, Object value, int sqlType) {
        return new Expr(GE, ListUtil.seq(name, value)).setOp2Wrapper(sqlType == SqlTypes.JSONB ? JSONB_CAST_TMPL : null);
    }

    public Expr ge(String name, Object value) {
        return ge(name, value, SqlTypes.ANY);
    }

    public Expr ge(String name, Object value, int sqlType) {
        return createAND(this, createGE(name, value, sqlType));
    }

    public Expr like(String name, Object value) {
        return like(name, value, SqlTypes.ANY);
    }

    public Expr like(String name, Object value, int sqlType) {
        return createLIKE(name, value, sqlType);
    }

    public static Expr createLIKE(String name, Object value) {
        return createLIKE(name, value, SqlTypes.ANY);
    }

    public static Expr createLIKE(String name, Object value, int sqlType) {
        return new Expr(LIKE, ListUtil.seq(name, value)).setOp2Wrapper(sqlType == SqlTypes.JSONB ? JSONB_CAST_TMPL : null);
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

    public static Expr createExists(Query<?> subQuery) {
        return new Expr(EXISTS, ListUtil.seq((Object)subQuery));
    }

    public static Expr createExists(String subQuery) {
        return new Expr(EXISTS, ListUtil.seq((Object) subQuery));
    }

    public static Expr createNot(Expr expr) {
        return new Expr(NOT, ListUtil.seq((Object) expr));
    }

    public static Expr createNotExists(Query<?> subQuery) {
        return createNot(createExists(subQuery));
    }

    public static Expr createNotExists(String subQuery) {
        return createNot(createExists(subQuery));
    }

    public Expr and(Expr other) {
        return createAND(this, other);
    }

    public Expr in(String property, Object value) {
        return createAND(this, createIN(property, value));
    }

    public Expr exists(Query<?> subQuery) {
        return createAND(this, createExists(subQuery));
    }

    public Expr exists(String subQuery) {
        return createAND(this, createExists(subQuery));
    }

    public Expr not(Expr expr) {
        return createAND(this, createNot(expr));
    }

    public Expr notExists(Query<?> subQuery) {
        return createAND(this, createNotExists(subQuery));
    }

    public Expr notExists(String subQuery) {
        return createAND(this, createNotExists(subQuery));
    }

    /**
     * whether this expr is `is null` or `is not null` sub sql
     *
     * @return
     */
    private boolean isNullOrNotNullExpr() {
        if (op.equalsIgnoreCase(EQ) || op.equalsIgnoreCase(NE)) {
            return items.size() == 2 && items.get(1) == null;
        } else {
            return false;
        }
    }

    private boolean isNotExpr() {
        return NOT.equalsIgnoreCase(op) && items.size() == 1 && items.get(0) instanceof Expr;
    }

    private boolean isExistsExpr() {
        return EXISTS.equalsIgnoreCase(op) && items.size() == 1;
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
        } else if(isNotExpr()) {
            Expr item1 = (Expr) items.get(0);
            QueryInfo subQueryInfo = item1.toQueryString(sqlMapper, query);
            return new QueryInfo("NOT (" + subQueryInfo.getQueryString() + ")", subQueryInfo.getParameterBindings());
        } else if(isExistsExpr()) {
            if(items.get(0) instanceof Query) {
                Query<?> item1 = (Query<?>) items.get(0);
                QueryInfo subQueryInfo = item1.toQuery();
                return new QueryInfo("EXISTS (SELECT 1 " + subQueryInfo.getQueryString() + ")", subQueryInfo.getParameterBindings());
            } else if(items.get(0) != null) {
                String item1 = items.get(0).toString();
                return new QueryInfo("EXISTS (" + item1 + ")", new ParameterBindings());
            } else {
                throw new JdbcRuntimeException("exists null wrong query sql");
            }
        }
        ParameterBindings bindings = new ParameterBindings();
        Object item1 = items.get(0);
        Object item2 = items.get(1);
        String queryStr = sqlMapper.getOpConditionSubSql(op, query.getSession().getEntityMetaOfClass(query.cls), item1, item2, op1Wrapper, op2Wrapper, bindings, null);
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
