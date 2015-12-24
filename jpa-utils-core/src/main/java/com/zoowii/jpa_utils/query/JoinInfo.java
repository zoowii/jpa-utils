package com.zoowii.jpa_utils.query;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * sql join表达式的信息
 * Created by zoowii on 2015/12/23.
 */
public class JoinInfo {
    public static final int INNER = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int OUT = 3;
    private String joinTableName;
    private String joinTableAlias;
    private int type = INNER;
    private List<Pair<String, String>> joinConditions = new ArrayList<Pair<String, String>>();

    public String getJoinTableName() {
        return joinTableName;
    }

    public void setJoinTableName(String joinTableName) {
        this.joinTableName = joinTableName;
    }

    public String getJoinTableAlias() {
        return joinTableAlias;
    }

    public void setJoinTableAlias(String joinTableAlias) {
        this.joinTableAlias = joinTableAlias;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Pair<String, String>> getJoinConditions() {
        return joinConditions;
    }

    public void setJoinConditions(List<Pair<String, String>> joinConditions) {
        this.joinConditions = joinConditions;
    }

    public JoinInfo on(String left, String right) {
        this.joinConditions.add(Pair.of(left, right));
        return this;
    }

    public static class Builder<M> {
        private JoinInfo joinInfo;
        private Query<M> query;

        public Builder(Query<M> query, JoinInfo joinInfo) {
            this.query = query;
            this.joinInfo = joinInfo;
        }

        public Query<M> endJoin() {
            return query;
        }

        public Builder on(String left, String right) {
            joinInfo = joinInfo.on(left, right);
            return this;
        }
    }
}
