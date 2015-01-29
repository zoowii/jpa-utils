package com.zoowii.jpa_utils.core;

import com.zoowii.jpa_utils.query.ParameterBindings;

import java.util.List;

/**
 * 封装底层orm实现的Query对象
 * Created by zoowii on 14-12-23.
 */
public interface IWrappedQuery {
    public IWrappedQuery setParameter(int index, Object value);

    public IWrappedQuery setParameter(String key, Object value);

    public IWrappedQuery setMaxResults(int limit);

    public IWrappedQuery setMaxResults(Session session, ParameterBindings parameterBindings, int limit);

    public IWrappedQuery setFirstResult(int offset);

    public IWrappedQuery setFirstResult(Session session, ParameterBindings parameterBindings, int offset);

    public List getResultList();

    public Object getSingleResult();

    public int executeUpdate();
}
