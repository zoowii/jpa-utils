package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.core.IWrappedTypedQuery;
import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.query.ParameterBindings;
import org.hibernate.Query;

import java.util.List;

/**
 * Created by zoowii on 14-12-23.
 */
public class HibernateQuery implements IWrappedTypedQuery {
    private final org.hibernate.Query originQuery;

    public HibernateQuery(Query originQuery) {
        this.originQuery = originQuery;
    }

    @Override
    public IWrappedQuery setParameter(int index, Object value) {
        return new HibernateQuery(originQuery.setParameter(index, value));
    }

    @Override
    public IWrappedQuery setParameter(String key, Object value) {
        return new HibernateQuery(originQuery.setParameter(key, value));
    }

    @Override
    public IWrappedQuery setMaxResults(int limit) {
        return new HibernateQuery(originQuery.setMaxResults(limit));
    }

    @Override
    public IWrappedQuery setMaxResults(Session session, ParameterBindings parameterBindings, int limit) {
        return setMaxResults(limit);
    }

    @Override
    public IWrappedQuery setFirstResult(int offset) {
        return new HibernateQuery(originQuery.setFirstResult(offset));
    }

    @Override
    public IWrappedQuery setFirstResult(Session session, ParameterBindings parameterBindings, int offset) {
        return setFirstResult(offset);
    }

    @Override
    public List getResultList() {
        return originQuery.list();
    }

    @Override
    public Object getSingleResult() {
        return originQuery.uniqueResult();
    }

    @Override
    public int executeUpdate() {
        return originQuery.executeUpdate();
    }
}
