package com.zoowii.jpa_utils.core;

import com.zoowii.jpa_utils.query.ParameterBindings;

import java.util.List;

/**
 * 封装底层orm实现的Query对象
 * Created by zoowii on 14-12-23.
 */
public interface IWrappedQuery {
    IWrappedQuery setParameter(int index, Object value);

    IWrappedQuery setParameter(String key, Object value);

    IWrappedQuery setParameter(int index, Object value, int sqlType);

    IWrappedQuery setParameter(String key, Object value, int sqlType);

    IWrappedQuery setMaxResults(int limit);

    IWrappedQuery setMaxResults(Session session, ParameterBindings parameterBindings, int limit);

    IWrappedQuery setFirstResult(int offset);

    IWrappedQuery setFirstResult(Session session, ParameterBindings parameterBindings, int offset);

    List getResultList();

    Object getSingleResult();

    int executeUpdate();
}
