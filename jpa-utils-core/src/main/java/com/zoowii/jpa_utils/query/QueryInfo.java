package com.zoowii.jpa_utils.query;

/**
 * Created by zoowii on 14-12-17.
 */
public class QueryInfo {
    private String queryString;
    private ParameterBindings parameterBindings = new ParameterBindings();
    private QueryExtras extras = new QueryExtras();

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public ParameterBindings getParameterBindings() {
        return parameterBindings;
    }

    public void setParameterBindings(ParameterBindings parameterBindings) {
        this.parameterBindings = parameterBindings;
    }

    public QueryExtras getExtras() {
        return extras;
    }

    public void setExtras(QueryExtras extras) {
        this.extras = extras;
    }

    public QueryInfo(String queryString, ParameterBindings parameterBindings, QueryExtras extras) {
        this.queryString = queryString;
        this.parameterBindings = parameterBindings;
        this.extras = extras;
    }

    public QueryInfo(String queryString, ParameterBindings parameterBindings) {
        this.queryString = queryString;
        this.parameterBindings = parameterBindings;
    }
}
