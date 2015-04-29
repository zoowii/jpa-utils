package com.zoowii.jpa_utils.query;

import com.google.common.base.Function;
import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.core.IWrappedTypedQuery;
import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
import com.zoowii.jpa_utils.orm.Model;
import com.zoowii.jpa_utils.util.ListUtil;
import com.zoowii.jpa_utils.util.ModelUtils;
import com.zoowii.jpa_utils.util.StringUtil;
import com.zoowii.jpa_utils.util.functions.Function2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Query<M> {
    protected String tableName = null;
    protected Class<?> cls = null;
    protected List<OrderBy> orderBys = new ArrayList<OrderBy>();
    protected Expr condition = Expr.dummy();
    protected String _tableSymbol = null;
    protected int _limit = -1;
    protected int _offset = -1;
    protected Map<Integer, Object> indexParameters = new HashMap<Integer, Object>();
    protected Map<String, Object> mapParameters = new HashMap<String, Object>();
    protected Session session;

    public Session getSession() {
        if (session != null) {
            return session;
        } else {
            return Model.getSession();
        }
    }

    public String getTableSymbol() {
        if (_tableSymbol == null) {
            _tableSymbol = StringUtil.randomString(5);
        }
        return _tableSymbol;
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
        query.orderBys = this.orderBys; // clone it
        query.indexParameters = this.indexParameters; // clone it
        query.mapParameters = this.mapParameters; // clone it
        return query;
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
        this.condition = this.condition.eq(name, value);
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

    public Query<M> or(Expr expr1, Expr expr2) {
        this.condition = this.condition.and(expr1.or(expr2));
        return this;
    }

    public Query<M> and(Expr expr) {
        this.condition = this.condition.and(expr);
        return this;
    }

    public Query<M> orderBy(String sort) {
        return orderBy(sort, true);
    }

    public Query<M> orderBy(String sort, boolean asc) {
        this.orderBys.add(new OrderBy(sort, asc));
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

    public QueryInfo toQuery() {
        SqlMapper sqlMapper = session.getSqlMapper();
        ModelMeta modelMeta = session.getEntityMetaOfClass(cls);
        String queryStr = sqlMapper.getFromSubSql(modelMeta, false).getLeft();
        QueryInfo exprQuery = this.condition != null ? this.condition.toQueryString(sqlMapper, this) : null;
        if (exprQuery != null) {
            queryStr += sqlMapper.getWhereSubSql(exprQuery.getQueryString());
        }
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
                return "select count(*) " + s;
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
        queryString = session.getSqlMapper().wrapQueryWithDefaultSelect(session.getEntityMetaOfClass(model), queryString);
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

    public M first() {
        return first(session);
    }

    public M first(Session session) {
        if (this.cls == null) {
            throw new RuntimeException("you need pass a model class");
        }
        return first(session, this.cls);
    }

    public M findUnique() {
        return findUnique(Model.getSession());
    }

    public M findUnique(Session session) {
        return first(session);
    }

    public M first(Class<?> model) {
        return first(Model.getSession(), model);
    }

    public M first(Session session, Class<?> model) {
        return (M) ListUtil.first(getTypedQuery(session, model).setMaxResults(1).getResultList());
    }

    public M single(Class<?> model) {
        return single(Model.getSession(), model);
    }

    public M single(Session session, Class<?> model) {
        return (M) getTypedQuery(session, model).setMaxResults(1).getSingleResult();
    }
}
