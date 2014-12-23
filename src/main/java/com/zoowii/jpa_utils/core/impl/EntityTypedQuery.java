package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.IWrappedQuery;
import com.zoowii.jpa_utils.core.IWrappedTypedQuery;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by zoowii on 14-12-23.
 */
public class EntityTypedQuery implements IWrappedTypedQuery {
    private final TypedQuery<?> originTypedQuery;

    public EntityTypedQuery(TypedQuery<?> originTypedQuery) {
        this.originTypedQuery = originTypedQuery;
    }

    @Override
    public IWrappedQuery setParameter(int index, Object value) {
        return new EntityTypedQuery(originTypedQuery.setParameter(index, value));
    }

    @Override
    public IWrappedQuery setParameter(String key, Object value) {
        return new EntityTypedQuery(originTypedQuery.setParameter(key, value));
    }

    @Override
    public IWrappedQuery setMaxResults(int limit) {
        return new EntityTypedQuery(originTypedQuery.setMaxResults(limit));
    }

    @Override
    public IWrappedQuery setFirstResult(int offset) {
        return new EntityTypedQuery(originTypedQuery.setFirstResult(offset));
    }

    @Override
    public List getResultList() {
        return originTypedQuery.getResultList();
    }

    @Override
    public Object getSingleResult() {
        return originTypedQuery.getSingleResult();
    }

    @Override
    public int executeUpdate() {
        return originTypedQuery.executeUpdate();
    }
}
