package com.zoowii.jpa_utils.extension;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Function;
import com.zoowii.jpa_utils.query.Expr;
import com.zoowii.jpa_utils.query.JoinInfo;
import com.zoowii.jpa_utils.query.Query;
import com.zoowii.jpa_utils.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * very simple paginator class, and you can use it if it's enought
 * Created by zoowii on 14-12-23.
 */
public class Paginator {
    protected long page = 1;
    protected long pageSize = 10;
    protected long total = 0;
    protected List<Pair<String, Boolean>> orders = new ArrayList<Pair<String, Boolean>>();
    protected List<Expr> expressions = new ArrayList<Expr>();
    protected Function<Query, Query> beforeQueryProcessor = null;
    protected List<Object> parameters = new ArrayList<Object>();
    protected String tableAlias;
    protected List<JoinInfo> joinInfos = new ArrayList<JoinInfo>();

    public long skippedCount() {
        return (page - 1) * pageSize;
    }

    public int getIntSkippedCount() {
        return (int) skippedCount();
    }

    public Paginator addExpression(Expr expr) {
        expressions.add(expr);
        return this;
    }

    /**
     * ==
     *
     * @param name property name
     * @param value eq to value
     * @return result instance
     */
    public Paginator eq(String name, Object value) {
        return addExpression(Expr.createEQ(name, value));
    }

    /**
     * great than
     *
     * @param name property name
     * @param value gt to value
     * @return result instance
     */
    public Paginator gt(String name, Object value) {
        return addExpression(Expr.createGT(name, value));
    }

    /**
     * less than
     *
     * @param name property name
     * @param value lt to value
     * @return result instance
     */
    public Paginator lt(String name, Object value) {
        return addExpression(Expr.createLT(name, value));
    }

    /**
     * !=
     *
     * @param name property name
     * @param value ne to value
     * @return result instance
     */
    public Paginator ne(String name, Object value) {
        return addExpression(Expr.createNE(name, value));
    }

    /**
     * great than or equal
     *
     * @param name property name
     * @param value gt to value
     * @return result instance
     */
    public Paginator ge(String name, Object value) {
        return addExpression(Expr.createGE(name, value));
    }

    /**
     * less than or equal
     *
     * @param name property name
     * @param value le to value
     * @return result instance
     */
    public Paginator le(String name, Object value) {
        return addExpression(Expr.createLE(name, value));
    }

    public Paginator like(String name, String value) {
        return addExpression(Expr.createLIKE(name, value));
    }

    public long getPageCount() {
        return 1 + (total - 1) / pageSize;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
        if (this.page <= 0) {
            this.page = 1;
        }
    }

    public long getPageSize() {
        return pageSize;
    }

    public int getIntPageSize() {
        return (int) getPageSize();
    }

    public int getIntTotal() {
        return (int) getTotal();
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
        if (this.total < 0) {
            this.total = 0;
        }
    }

    public List<Pair<String, Boolean>> getOrders() {
        return orders;
    }

    public void setOrders(List<Pair<String, Boolean>> orders) {
        this.orders = orders;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    public List<Expr> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Expr> expressions) {
        this.expressions = expressions;
    }

    @JSONField(serialize = false, deserialize = false)
    @JsonIgnore
    public Function<Query, Query> getBeforeQueryProcessor() {
        return beforeQueryProcessor;
    }

    @JSONField(serialize = false, deserialize = false)
    @JsonIgnore
    public void setBeforeQueryProcessor(Function<Query, Query> beforeQueryProcessor) {
        this.beforeQueryProcessor = beforeQueryProcessor;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    public <M> JoinInfo.Builder<M> join(String tableName, String tableAlias, int joinType) {
        if (StringUtil.isEmpty(tableAlias)) {
            tableAlias = tableName;
        }
        if (StringUtil.isEmpty(tableName)) {
            return new JoinInfo.Builder<M>(null, null);
        }
        JoinInfo joinInfo = new JoinInfo();
        joinInfo.setType(joinType);
        joinInfo.setJoinTableName(tableName);
        joinInfo.setJoinTableAlias(tableAlias);
        this.joinInfos.add(joinInfo);
        return new JoinInfo.Builder<M>(null, joinInfo);
    }

    public List<JoinInfo> getJoinInfos() {
        return joinInfos;
    }
}
