package com.zoowii.jpa_utils.core;

import com.zoowii.jpa_utils.query.Expr;
import com.zoowii.jpa_utils.query.ParameterBindings;
import com.zoowii.jpa_utils.query.QueryInfo;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zoowii on 14-12-23.
 */
public abstract class AbstractSession implements Session {

    /**
     * 用来解决事务嵌套
     */
    protected final Queue<Object> txStack = new ConcurrentLinkedQueue<Object>();

    @Override
    public void begin() {
        txStack.add(1);
        if (getTransactionNestedLevel() > 1) {
            return;
        }
        getTransaction().begin();
    }

    @Override
    public void commit() {
        if (!isOpen()) {
            begin();
        }
        txStack.poll();
        if (getTransactionNestedLevel() > 0) {
            return;
        }
        getTransaction().commit();
    }


    @Override
    public int delete(Class<?> model, Expr expr) {
        QueryInfo exprQuery = expr.toQueryString();
        String sql = String.format("delete from %s where (%s)", model.getSimpleName(), exprQuery.getQueryString());
        IWrappedQuery query = createQuery(sql);
        ParameterBindings parameterBindings = exprQuery.getParameterBindings();
        if (parameterBindings != null) {
            for (int i = 0; i < parameterBindings.getIndexBindings().size(); ++i) {
                query.setParameter(i + 1, parameterBindings.getIndexBindings().get(i));
            }
            for (String key : parameterBindings.getMapBindings().keySet()) {
                query.setParameter(key, parameterBindings.getMapBindings().get(key));
            }
        }
        return query.executeUpdate();
    }

    @Override
    public void rollback() {
        if (!isTransactionActive()) {
            return;
        }
        txStack.poll();
        if (getTransactionNestedLevel() > 0) {
            return;
        }
        getTransaction().rollback();
    }

    @Override
    public boolean isClosed() {
        return !isOpen();
    }

    @Override
    public int getTransactionNestedLevel() {
        return txStack.size();
    }

    @Override
    public boolean isTransactionActive() {
        return getTransaction().isActive();
    }

}
