package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.core.IWrappedTypedQuery;

import javax.persistence.Query;
import java.util.List;

/**
 * Created by zoowii on 14-12-23.
 */
public class EntityQuery implements IWrappedQuery {
    private final javax.persistence.Query originQuery;

    public EntityQuery(Query originQuery) {
        this.originQuery = originQuery;
    }

    @Override
    public IWrappedQuery setParameter(int index, Object value) {
        return new EntityQuery(originQuery.setParameter(index, value));
    }

    @Override
    public IWrappedQuery setParameter(String key, Object value) {
        return new EntityQuery(originQuery.setParameter(key, value));
    }

    @Override
    public IWrappedQuery setMaxResults(int limit) {
        return new EntityQuery(originQuery.setMaxResults(limit));
    }

    @Override
    public IWrappedQuery setFirstResult(int offset) {
        return new EntityQuery(originQuery.setFirstResult(offset));
    }

    @Override
    public List getResultList() {
        return originQuery.getResultList();
    }

    @Override
    public Object getSingleResult() {
        return originQuery.getSingleResult();
    }

    @Override
    public int executeUpdate() {
        return originQuery.executeUpdate();
    }
}
