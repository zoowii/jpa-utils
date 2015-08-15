package com.zoowii.jpa_utils.builders;

import com.zoowii.jpa_utils.query.ParameterBindings;
import com.zoowii.jpa_utils.query.Query;
import com.zoowii.jpa_utils.query.QueryInfo;
import com.zoowii.jpa_utils.util.ListUtil;
import com.zoowii.jpa_utils.util.StringUtil;

import java.util.Map;

/**
 * Created by zoowii on 2015/8/16.
 */
public class QuerySqlBuilder {
    private Query<?> query;
    private ParameterBindings parameterBindings = new ParameterBindings();
    private StringBuilder sql = new StringBuilder();

    public QuerySqlBuilder(Query<?> query) {
        this.query = query;
    }

    public QuerySqlBuilder update(String... setQuerys) {
        sql.append("UPDATE ");
        sql.append(query.table());
        sql.append(" SET ");
        sql.append(StringUtil.join(ListUtil.seq(setQuerys), ","));
        sql.append(" ");
        return this;
    }

    public QuerySqlBuilder delete() {
        sql.append("DELETE FROM ");
        sql.append(query.table());
        sql.append(" ");
        return this;
    }

    public QuerySqlBuilder set(Object... keyValueParams) {
        Map<String, Object> keyValueBindingPairs = ListUtil.hashmap(keyValueParams);
        for (String key : keyValueBindingPairs.keySet()) {
            parameterBindings.addBinding(key, keyValueBindingPairs.get(key));
        }
        return this;
    }

    public QuerySqlBuilder where(String more) {
        QueryInfo whereQueryInfo = query.toWhereQueryInfo();
        this.parameterBindings.extend(whereQueryInfo.getParameterBindings());
        sql.append(query.where(whereQueryInfo.getQueryString()));
        sql.append(" ");
        if (!StringUtil.isEmpty(more)) {
            sql.append(more);
            sql.append(" ");
        }
        return this;
    }

    public QuerySqlBuilder where() {
        return this.where("");
    }

    public int executeUpdate() {
        return query.executeUpdateQueryInfo(sql.toString(), parameterBindings);
    }
}
