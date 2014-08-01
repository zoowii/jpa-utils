package com.zoowii.hibernate_utils.query;

import com.google.common.base.Function;
import com.zoowii.hibernate_utils.orm.Model;
import com.zoowii.hibernate_utils.util.ListUtil;
import com.zoowii.hibernate_utils.util.StringUtil;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Query<M extends Model> {
    protected String tableName = null;  // TODO: select from multi-tables
    protected Class cls = null;
    protected List<OrderBy> orderBys = new ArrayList<OrderBy>();
    protected Expr condition = Expr.dummy();
    protected String _tableSymbol = null;
    protected int _limit = -1;
    protected int _offset = -1;

    public String getTableSymbol() {
        if (_tableSymbol == null) {
            _tableSymbol = StringUtil.randomString(5);
        }
        return _tableSymbol;
    }

    /**
     * @param tableName maybe `User` or `User user`(then you can use user.name='abc' in expr)
     */
    public Query(String tableName) {
        this.tableName = tableName;
    }

    public Query(Class cls) {
        this.cls = cls;
        this.tableName = cls.getSimpleName();
    }

    public Query<M> limit(int limit) {
        _limit = limit;
        return this;
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
        Query<M> query = new Query<M>(tableName);
        query._limit = this._limit;
        query._offset = this._offset;
        query._tableSymbol = this._tableSymbol;
        query.condition = this.condition;
        query.orderBys = this.orderBys;
        return query;
    }

    public Query<M> eq(String name, Object value) {
        this.condition = this.condition.eq(name, value);
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

    public Map<String, Object> toQuery() {
        String queryStr = "from " + tableName + " ";
        Map<String, Object> exprQuery = this.condition != null ? this.condition.toQueryString(this) : null;
        if (exprQuery != null) {
            queryStr += " where " + exprQuery.get("query");
        }
        if (this.orderBys.size() > 0) {
            queryStr += " order by " + this.getOrderByString();
        }
        List<Object> bindings = ListUtil.seq();
        if (exprQuery != null) {
            bindings = (List<Object>) exprQuery.get("bindings");
        }
        Map<String, Object> extras = ListUtil.hashmap("dummy", "dummy");
        if (this._limit >= 0) {
            extras.put("max", this._limit);
        }
        if (this._offset >= 0) {
            extras.put("offset", this._offset);
        }
        return ListUtil.hashmap("query", queryStr, "bindings", bindings, "extras", extras);
    }

    public long count() {
        if (this.cls == null) {
            throw new RuntimeException("you need pass a model class");
        }
        return count(this.cls);
    }

    public long count(Class model) {
        return (Long) getTypedQuery(Long.class, new Function<String, String>() {
            @Override
            public String apply(String s) {
                return "select count(*) " + s;
            }
        }).getSingleResult();
    }

    public List<M> all() {
        if (this.cls == null) {
            throw new RuntimeException("you need pass a model class");
        }
        return all(this.cls);
    }

    public TypedQuery getTypedQuery(Class model) {
        return getTypedQuery(model, null);
    }

    /**
     * @param model        要查询的model
     * @param queryWrapper 用来对HQL进行二次处理,处理后再用来执行
     */
    public TypedQuery getTypedQuery(Class model, Function<String, String> queryWrapper) {
        Map<String, Object> query = this.toQuery();
        String queryString = (String) query.get("query");
        if (queryWrapper != null) {
            queryString = queryWrapper.apply(queryString);
        }
        TypedQuery typedQuery = M.getSession().getEntityManager().createQuery(queryString, model);
        List<Object> bindings = (List<Object>) query.get("bindings");
        if (bindings != null) {
            for (int i = 0; i < bindings.size(); ++i) {
                typedQuery.setParameter(i + 1, bindings.get(i));
            }
        }
        Map<String, Object> extras = (Map<String, Object>) query.get("extras");
        if (extras != null) {
            if (extras.containsKey("max")) {
                typedQuery.setMaxResults((Integer) extras.get("max"));
            }
            if (extras.containsKey("offset")) {
                typedQuery.setMaxResults((Integer) extras.get("offset"));
            }
        }
        return typedQuery;
    }

    public List<M> all(Class model) {
        return getTypedQuery(model).getResultList();
    }

    public M first() {
        if (this.cls == null) {
            throw new RuntimeException("you need pass a model class");
        }
        return first(this.cls);
    }

    public M first(Class model) {
        return (M) getTypedQuery(model).setMaxResults(1).getSingleResult();
    }

}
