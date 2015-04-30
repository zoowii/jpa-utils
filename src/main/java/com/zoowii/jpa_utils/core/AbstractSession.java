package com.zoowii.jpa_utils.core;

import com.zoowii.jpa_utils.core.impl.EntitySessionFactory;
import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.query.Expr;
import com.zoowii.jpa_utils.query.ParameterBindings;
import com.zoowii.jpa_utils.query.QueryInfo;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zoowii on 14-12-23.
 */
public abstract class AbstractSession implements Session {

    /**
     * to solve problem of nested transaction
     */
    protected final Queue<Object> txStack = new ConcurrentLinkedQueue<Object>();

    @Override
    public int getIndexParamBaseOrdinal() {
        return 1;
    }

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
        QueryInfo exprQuery = expr.toQueryString(getSqlMapper());
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

    private static final Map<Class<?>, ModelMeta> ENTITY_META_CACHE = new HashMap<Class<?>, ModelMeta>();

    @Override
    public synchronized ModelMeta getEntityMetaOfClass(Class<?> entityCls) {
        if(ENTITY_META_CACHE.containsKey(entityCls)) {
            return ENTITY_META_CACHE.get(entityCls);
        }
        ModelMeta modelMeta = new ModelMeta(entityCls, getSqlMapper());
        ENTITY_META_CACHE.put(entityCls, modelMeta);
        return modelMeta;
    }

    /**
     * manually bind session to current thread
     */
    private static final ThreadLocal<WeakReference<Session>> defaultThreadLocalSessions = new ThreadLocal<WeakReference<Session>>();

    /**
     * check whether session binded to current thread first, if not, use EntitySessionFactory.currentSession(),
     * and set result to binded session of current thread
     *
     * @return current session of current thread
     */
    public static Session currentSession() {
        WeakReference<Session> defaultSession = defaultThreadLocalSessions.get();
        if (defaultSession != null && defaultSession.get() != null) {
            return defaultSession.get();
        }
        Session session = EntitySessionFactory.getDefaultEntitySessionFactory().currentSession();
        defaultThreadLocalSessions.set(new WeakReference<Session>(session));
        return session;
    }

    public static Session bindCurrentSession(Session session) {
        defaultThreadLocalSessions.set(new WeakReference<Session>(session));
        return session;
    }

    public static Session getSession(String persistenceUnit) {
        return EntitySessionFactory.getEntitySessionFactory(persistenceUnit).currentSession();
    }

    @Override
    public Session asThreadLocal() {
        return bindCurrentSession(this);
    }

    @Override
    public String columnNameInQuery(Class<?> modelCls, String propertyName) {
        return propertyName;
    }
}
