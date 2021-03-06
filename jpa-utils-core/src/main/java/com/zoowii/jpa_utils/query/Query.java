package com.zoowii.jpa_utils.query;

import com.google.common.base.Function;
import com.zoowii.jpa_utils.builders.QuerySqlBuilder;
import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.enums.SqlTypes;
import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
import com.zoowii.jpa_utils.orm.Model;
import com.zoowii.jpa_utils.util.ListUtil;
import com.zoowii.jpa_utils.util.StringUtil;
import com.zoowii.jpa_utils.util.functions.Function2;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Query<M> {
    protected String tableName = null;
    protected Class<?> cls = null;
    private Map<String, String> selectColumns = new HashMap<String, String>();
    protected List<OrderBy> orderBys = new ArrayList<OrderBy>();
    protected Expr condition = Expr.dummy();
    protected transient String _tableSymbol = null;
    protected int _limit = -1;
    protected int _offset = -1;
    protected Map<Integer, Object> indexParameters = new HashMap<Integer, Object>();
    protected Map<String, Object> mapParameters = new HashMap<String, Object>();
    protected List<JoinInfo> joinInfos = new ArrayList<JoinInfo>();
    protected Session session;
    protected static AtomicLong generatedNameCount = new AtomicLong(0L);
    protected boolean useAlias = false;
    protected String extraSql = "";
    protected String groupBySql = "";

    public Class<?> getModelClass() {
        return cls;
    }

    public Session getSession() {
        if (session != null) {
            return session;
        } else {
            return Model.getSession();
        }
    }

    public Query<M> setExtraSql(String sql) {
        this.extraSql = sql != null ? sql : "";
        return this;
    }

    public Query<M> setGroupBySql(String sql) {
        this.groupBySql = sql != null ? sql : "";
        return this;
    }

    public String getExtraSql() {
        return extraSql;
    }

    public String getGroupBySql() {
        return groupBySql;
    }

    public Query<M> setSession(Session session) {
        this.session = session;
        return this;
    }

    public String generateRandomTableAliasName() {
        return "table_alias_" + generatedNameCount.addAndGet(1);
    }

    public String getTableSymbol() {
        if (_tableSymbol == null) {
            _tableSymbol = generateRandomTableAliasName();
        }
        return _tableSymbol;
    }

    public String getUsingTableSymbol() {
        if(_tableSymbol != null) {
            return _tableSymbol;
        } else {
            return getSession().getSqlMapper().tableName(getSession().getEntityMetaOfClass(cls));
        }
    }

    /**
     * @param tableName maybe `User` or `User user`(then you can use user.name='abc' in expr)
     * @param session which session the query use
     */
    public Query(String tableName, Session session) {
        this.tableName = tableName;
        this.session = session;
    }

    public Query(Class<?> cls, Session session) {
        this.cls = cls;
        this.session = session;
        this.tableName = cls.getSimpleName(); // 这里使用类名而不是@Table(name=...)是因为HQL使用的是类名
    }

    public Query<M> limit(int limit) {
        _limit = limit;
        return this;
    }

    public Query<M> setMaxRows(int limit) {
        return limit(limit);
    }

    public Query<M> offset(int offset) {
        this._offset = offset;
        return this;
    }

    public Query<M> where(Expr expr) {
        this.condition = expr;
        return this;
    }

    public Query<M> clone() {
        Query<M> query = new Query<M>(tableName, session);
        query._limit = this._limit;
        query._offset = this._offset;
        query._tableSymbol = this._tableSymbol;
        query.condition = this.condition;
        query.orderBys = new ArrayList<OrderBy>();
        query.orderBys.addAll(this.orderBys);
        query.indexParameters = new HashMap<Integer, Object>();
        query.indexParameters.putAll(this.indexParameters);
        query.mapParameters = new HashMap<String, Object>();
        query.mapParameters.putAll(this.mapParameters);
        query.selectColumns = new HashMap<String, String>();
        query.selectColumns.putAll(this.selectColumns);
        query.joinInfos = new ArrayList<JoinInfo>();
        query.joinInfos.addAll(this.joinInfos);
        query.cls = this.cls;
        query.useAlias = this.useAlias;
        query.session = this.session;
        query.extraSql = this.extraSql;
        query.groupBySql = this.groupBySql;
        return query;
    }

    public Query<M> select(String property, String asProperty) {
        SqlMapper sqlMapper = getSession().getSqlMapper();
        ModelMeta modelMeta = getSession().getEntityMetaOfClass(cls);
        ModelMeta.ModelColumnMeta modelColumnMeta = modelMeta.getColumnMetaByFieldName(property);
        if (modelColumnMeta == null) {
            this.selectColumns.put(property, asProperty);
        } else {
            this.selectColumns.put(sqlMapper.getSqlColumnNameWrapped(modelColumnMeta.columnName), asProperty);
        }
        return this;
    }

    public Query<M> select(String property) {
        SqlMapper sqlMapper = getSession().getSqlMapper();
        ModelMeta modelMeta = getSession().getEntityMetaOfClass(cls);
        ModelMeta.ModelColumnMeta modelColumnMeta = modelMeta.getColumnMetaByFieldName(property);
        if (modelColumnMeta == null) {
            this.selectColumns.put(property, property);
        } else {
            this.selectColumns.put(sqlMapper.getSqlColumnNameWrapped(modelColumnMeta.columnName), modelColumnMeta.columnName);
        }
        return this;
    }

    public JoinInfo.Builder outJoin(String tableName, String tableAlias) {
        return join(tableName, tableAlias, JoinInfo.OUT);
    }

    public JoinInfo.Builder rightJoin(String tableName, String tableAlias) {
        return join(tableName, tableAlias, JoinInfo.RIGHT);
    }

    public JoinInfo.Builder leftJoin(String tableName, String tableAlias) {
        return join(tableName, tableAlias, JoinInfo.LEFT);
    }

    public JoinInfo.Builder innerJoin(String tableName, String tableAlias) {
        return join(tableName, tableAlias, JoinInfo.INNER);
    }

    public JoinInfo.Builder join(String tableName, String tableAlias) {
        return join(tableName, tableAlias, JoinInfo.INNER);
    }

    public JoinInfo.Builder join(String tableName, String tableAlias, int joinType) {
        if (StringUtil.isEmpty(tableAlias)) {
            tableAlias = tableName;
        }
        if (StringUtil.isEmpty(tableName)) {
            return new JoinInfo.Builder<M>(this, null);
        }
        JoinInfo joinInfo = new JoinInfo();
        joinInfo.setType(joinType);
        joinInfo.setJoinTableName(tableName);
        joinInfo.setJoinTableAlias(tableAlias);
        this.joinInfos.add(joinInfo);
        return new JoinInfo.Builder<M>(this, joinInfo);
    }

    public Query<M> addJoinInfo(JoinInfo joinInfo) {
        if(joinInfo!=null) {
            this.joinInfos.add(joinInfo);
        }
        return this;
    }

    public Query<M> setParameter(String key, Object value) {
        this.mapParameters.put(key, value);
        return this;
    }

    public Query<M> setParameter(int index, Object value) {
        this.indexParameters.put(index, value);
        return this;
    }

    public Query<M> eq(String name, Object value) {
        return eq(name, value, SqlTypes.ANY);
    }

    public Query<M> eq(String name, Object value, int sqlType) {
        this.condition = this.condition.eq(name, value, sqlType);
        return this;
    }
    
    public Query<M> in(String property, Object value) {
        this.condition = this.condition.in(property, value);
        return this;
    }

    public Query<M> ne(String name, Object value) {
        this.condition = this.condition.ne(name, value);
        return this;
    }

    public Query<M> gt(String name, Object value) {
        this.condition = this.condition.gt(name, value);
        return this;
    }

    public Query<M> alias(String aliasTableName) {
        this._tableSymbol = aliasTableName;
        useAlias = true;
        return this;
    }

    public Query<M> alias() {
        getTableSymbol();
        useAlias = true;
        return this;
    }

    /**
     * direct query sql string
     * 在.gt(prop, Query.sql(...))等查询中,作为非parameter参数,而是直接并入到query sql中
     * @param sql
     * @return
     */
    public static DirectSql directSql(String sql) {
        return new DirectSql(sql);
    }

    public Query<M> ge(String name, Object value) {
        this.condition = this.condition.ge(name, value);
        return this;
    }

    public Query<M> like(String name, Object value) {
        this.condition = this.condition.like(name, value);
        return this;
    }

    public Query<M> lt(String name, Object value) {
        this.condition = this.condition.lt(name, value);
        return this;
    }

    public Query<M> le(String name, Object value) {
        this.condition = this.condition.le(name, value);
        return this;
    }

    public Query<M> exists(Query<?> subQuery) {
        this.condition = this.condition.exists(subQuery);
        return this;
    }

    public Query<M> exists(String subQuery) {
        this.condition = this.condition.exists(subQuery);
        return this;
    }

    public Query<M> not(Expr expr) {
        this.condition = this.condition.not(expr);
        return this;
    }

    public Query<M> notExists(Query<?> subQuery) {
        this.condition = this.condition.notExists(subQuery);
        return this;
    }

    public Query<M> notExists(String subQuery) {
        this.condition = this.condition.notExists(subQuery);
        return this;
    }

    public Query<M> or(Expr... exprs) {
        if(exprs.length<1) {
            return this;
        }
        Expr curExpr = exprs[0];
        for(int i=1;i<exprs.length;++i) {
            curExpr = curExpr.or(exprs[i]);
        }
        this.condition = this.condition.and(curExpr);
        return this;
    }

//    public Query<M> or(Expr expr1, Expr expr2) {
//        this.condition = this.condition.and(expr1.or(expr2));
//        return this;
//    }

    public Query<M> and(Expr expr) {
        this.condition = this.condition.and(expr);
        return this;
    }

    public Query<M> orderBy(String sort) {
        return orderBy(sort, true);
    }

    public Query<M> orderBy(String sort, boolean asc) {
        this.orderBys.add(new OrderBy(getSession().columnNameInQuery(cls, sort), asc));
        return this;
    }

    public Query<M> asc(String sort) {
        return orderBy(sort, true);
    }

    public Query<M> desc(String sort) {
        return orderBy(sort, false);
    }

    public String getOrderByString() {
        final Query _this = this;
        List<String> orderByStrs = ListUtil.map(this.orderBys, new Function<OrderBy, String>() {
            @Override
            public String apply(OrderBy orderBy) {
                return orderBy != null ? orderBy.toOrderByString(_this) : null;
            }
        });
        return StringUtil.join(orderByStrs, ",");
    }

    public String where(String subQueryCondition) {
        SqlMapper sqlMapper = getSession().getSqlMapper();
        return sqlMapper.getWhereSubSql(subQueryCondition);
    }

    public String table() {
        SqlMapper sqlMapper = getSession().getSqlMapper();
        ModelMeta modelMeta = getSession().getEntityMetaOfClass(cls);
        return sqlMapper.tableName(modelMeta);
    }

    public QueryInfo toWhereQueryInfo() {
        SqlMapper sqlMapper = getSession().getSqlMapper();
        return this.condition != null ? this.condition.toQueryString(sqlMapper, this) : null;
    }

    public int executeUpdateQueryInfo(QueryInfo queryInfo) {
        return executeUpdateQueryInfo(queryInfo.getQueryString(), queryInfo.getParameterBindings());
    }

    public QuerySqlBuilder sqlBuilder() {
        return new QuerySqlBuilder(this);
    }

    public int executeUpdateQueryInfo(String sql, ParameterBindings bindings) {
        IWrappedQuery typedQuery = getSession().createQuery(sql);
        if (bindings != null) {
            for (int i = 0; i < bindings.getIndexBindings().size(); ++i) {
                typedQuery = typedQuery.setParameter(i + getSession().getIndexParamBaseOrdinal(), bindings.getIndexBindings().get(i));
            }
            for (String key : bindings.getMapBindings().keySet()) {
                typedQuery = typedQuery.setParameter(key, bindings.getMapBindings().get(key));
            }
        }
        return typedQuery.executeUpdate();
    }

    public QueryInfo toQuery() {
        SqlMapper sqlMapper = getSession().getSqlMapper();
        ModelMeta modelMeta = getSession().getEntityMetaOfClass(cls);
        String queryStr = sqlMapper.getFromSubSql(modelMeta, useAlias, getUsingTableSymbol()).getLeft();
        if (joinInfos.size() > 0) {
            for (JoinInfo joinInfo : joinInfos) {
                if (joinInfo.getJoinConditions().size() < 1) {
                    continue;
                }
                String joinSql = " " + sqlMapper.getOfJoin(joinInfo) + " ";
                for (Pair<String, String> joinCond : joinInfo.getJoinConditions()) {
                    joinSql += " " + sqlMapper.getOfJoinOn(joinCond);
                }
                queryStr += joinSql + " ";
            }
        }
        QueryInfo exprQuery = this.condition != null ? this.condition.toQueryString(sqlMapper, this) : null;
        if (exprQuery != null) {
            queryStr += sqlMapper.getWhereSubSql(exprQuery.getQueryString());
        }
        queryStr += this.groupBySql + " ";
        if (this.orderBys.size() > 0) {
            queryStr += sqlMapper.getOrderBySubSql(this.getOrderByString());
        }
        ParameterBindings bindings = new ParameterBindings();
        if (exprQuery != null) {
            bindings = exprQuery.getParameterBindings();
        }
        QueryExtras extras = new QueryExtras();
        if (this._limit >= 0) {
            extras.setMax(this._limit);
        }
        if (this._offset >= 0) {
            extras.setOffset(this._offset);
        }
        queryStr += extraSql;
        return new QueryInfo(queryStr, bindings, extras);
    }

    public long count(Session session) {
        if (this.cls == null) {
            throw new RuntimeException("you need pass a model class");
        }
        return count(session, this.cls);
    }

    public long count() {
        return count(session);
    }

    public long count(Session session, Class<?> model) {
        // can't use getSingleResult simply, because in distributed mysql cluster, count sql may return multi rows
        List result = getTypedQuery(session, Long.class, new Function<String, String>() {
            @Override
            public String apply(String s) {
                s = s.trim();
                if(s.toUpperCase().startsWith("FROM")) {
                    return "SELECT COUNT(1) " + s;
                } else {
                    String alias = (getTableSymbol() + "_alias_" + StringUtil.randomString(5)).toUpperCase();
                    return String.format("SELECT COUNT(1) FROM (%s) %s", s, alias);
                }
            }
        }).getResultList();
        return (Long) ListUtil.reduce(result, 0L, new Function2() {
            @Override
            public Object apply(Object o1, Object o2) {
                if (o1 == null && o2 == null) {
                    return 0L;
                }
                if (o1 == null) {
                    return o2;
                }
                return (Long) o1 + (Long) o2;
            }
        });
    }

    public long count(Class<?> model) {
        return count(session, model);
    }

    public List<M> all() {
        return all(session);
    }

    public List<M> all(Session session) {
        if (this.cls == null) {
            throw new RuntimeException("you need pass a model class");
        }
        return all(session, this.cls);
    }

    public IWrappedQuery getTypedQuery(Session session, Class<?> model) {
        return getTypedQuery(session, model, null);
    }

    public IWrappedQuery getTypedQuery(Class<?> model) {
        return getTypedQuery(Model.getSession(), model);
    }

    public IWrappedQuery getTypedQuery(Class<?> model, Function<String, String> queryWrapper) {
        return getTypedQuery(session, model, queryWrapper);
    }

    /**
     * @param session use which session
     * @param model        to model to query
     * @param queryWrapper post-processor to process HQL string, before execute the HQL
     * @return typed query
     */
    public IWrappedQuery getTypedQuery(Session session, Class<?> model, Function<String, String> queryWrapper) {
        QueryInfo query = this.toQuery();
        String queryString = query.getQueryString();
        queryString = session.getSqlMapper().wrapQueryWithSelect(session.getEntityMetaOfClass(model), queryString, this.selectColumns);
        if (queryWrapper != null) {
            queryString = queryWrapper.apply(queryString);
        }
        IWrappedQuery typedQuery = session.createQuery(model, queryString);
        QueryExtras extras = query.getExtras();
        ParameterBindings bindings = query.getParameterBindings();
        if (extras != null) {
            if (extras.getMax() >= 0) {
                typedQuery = typedQuery.setMaxResults(session, bindings, extras.getMax());
            }
            if (extras.getOffset() >= 0) {
                typedQuery = typedQuery.setFirstResult(session, bindings, extras.getOffset());
            }
        }
        if (bindings != null) {
            for (int i = 0; i < bindings.getIndexBindings().size(); ++i) {
                typedQuery = typedQuery.setParameter(i + session.getIndexParamBaseOrdinal(), bindings.getIndexBindings().get(i));
            }
            for (String key : bindings.getMapBindings().keySet()) {
                typedQuery = typedQuery.setParameter(key, bindings.getMapBindings().get(key));
            }
        }
        return typedQuery;
    }

    public List<M> findList() {
        return findList(session);
    }

    public List<M> findList(Session session) {
        return all(session);
    }

    public List<M> all(Class<?> model) {
        return all(session, model);
    }

    public List<M> all(Session session, Class<?> model) {
        return getTypedQuery(session, model).getResultList();
    }

    public <T> List<T> allSelected(Class<? extends T> model) {
        return allSelected(session, model);
    }

    public <T> List<T> allSelected(Session session, Class<? extends T> model) {
        return getTypedQuery(session, model).getResultList();
    }

    public M first() {
        return first(session);
    }

    public M first(Session session) {
        if (this.cls == null) {
            throw new RuntimeException("you need pass a model class");
        }
        return first(session, this.cls);
    }

    public M first(Class<?> model) {
        return first(Model.getSession(), model);
    }

    public M first(Session session, Class<?> model) {
        return (M) ListUtil.first(getTypedQuery(session, model).setMaxResults(1).getResultList());
    }

    public <T> T firstSelected(Class<? extends T> model) {
        return firstSelected(Model.getSession(), model);
    }

    public <T> T firstSelected(Session session, Class<? extends T> model) {
        return (T) ListUtil.first(getTypedQuery(session, model).setMaxResults(1).getResultList());
    }

    public M findUnique() {
        return findUnique(Model.getSession());
    }

    public M findUnique(Session session) {
        return first(session);
    }

    public M findUnique(Class<?> model) {
        return first(model);
    }

    public M findUnique(Session session, Class<?> model) {
        return first(session, model);
    }

    public <T> T findUniqueSelected(Class<? extends T> model) {
        return firstSelected(model);
    }

    public <T> T findUniqueSelected(Session session, Class<? extends T> model) {
        return firstSelected(session, model);
    }

    public M single(Class<?> model) {
        return single(Model.getSession(), model);
    }

    public M single(Session session, Class<?> model) {
        return (M) getTypedQuery(session, model).setMaxResults(1).getSingleResult();
    }
}
