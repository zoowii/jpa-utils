package com.zoowii.jpa_utils.core;

import com.zoowii.jpa_utils.core.impl.EntitySessionFactory;
import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.query.Expr;
import com.zoowii.jpa_utils.query.ParameterBindings;
import com.zoowii.jpa_utils.query.QueryInfo;
import com.zoowii.jpa_utils.util.Logger;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
    public boolean isRunning() {
        return txStack.size() > 0;
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
    public void startBatch() {

    }

    @Override
    public void endBatch() {

    }

    @Override
    public int[] executeBatch() {
        return new int[0];
    }

    @Override
    public void updateBatch(List<Object> entities) {
        for (Object entity : entities) {
            update(entity);
        }
    }

    @Override
    public void saveBatch(List<Object> entities) {
        for (Object entity : entities) {
            save(entity);
        }
    }

    @Override
    public void deleteBatch(List<Object> entities) {
        for (Object entity : entities) {
            delete(entity);
        }
    }

    /**
     * start first-level cache(cache lifecycle in session)
     */
    @Override
    public void startCache() {

    }

    @Override
    public void endCache() {

    }

    /**
     * start second-level cache(cache lifecycle in session factory)
     */
    @Override
    public void startSecondLevelCache() {

    }

    @Override
    public void endSecondLevelCache() {

    }

    @Override
    public boolean isStartedCache() {
        return false;
    }

    @Override
    public boolean isStartedSecondLevelCache() {
        return false;
    }

    @Override
    public void cacheBean(Object key, Class<?> beanCls, Object bean) {

    }

    @Override
    public void cacheBeanInSecondLevel(Object key, Class<?> beanCls, Object bean) {}

    @Override
    public void removeBeanCache(Object key, Class<?> beanCls) {

    }


    /**
     * find in firstLevelCache first, if not found, find in secondLevelCache then
     * @param key
     * @return
     */
    @Override
    public <T> T getCachedBean(Object key, Class<? extends T> cls) {
        return null;
    }


    @Override
    public int delete(Class<?> model, Expr expr) {
        QueryInfo exprQuery = expr.toQueryString(getSqlMapper());
        String sql = String.format("delete from %s where (%s)", model.getSimpleName(), exprQuery.getQueryString());
        Logger.info("delete sql: " + sql);
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
    public void closeFully() {
        txStack.clear();
        close();
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
        ModelMeta modelMeta = ModelMeta.getModelMeta(entityCls, getSqlMapper());
        ENTITY_META_CACHE.put(entityCls, modelMeta);
        return modelMeta;
    }

    /**
     * manually bind session to current thread
     */
    private static final ThreadLocal<WeakReference<Session>> defaultThreadLocalSessions = new ThreadLocal<WeakReference<Session>>();

    private static transient SessionFactory defaultSessionFactory = null;

    public static void setDefaultSessionFactory(SessionFactory sessionFactory) {
        defaultSessionFactory = sessionFactory;
    }

    public static void setDefaultSessionFactoryIfEmpty(SessionFactory sessionFactory) {
        if (sessionFactory == null) {
            return;
        }
        if (defaultSessionFactory == null) {
            synchronized (AbstractSession.class) {
                if (defaultSessionFactory == null) {
                    defaultSessionFactory = sessionFactory;
                }
            }
        }
    }

    /**
     * get session binded to current thread, if not exists, return null
     * @return
     */
    public static Session currentSessionIfExists() {
        WeakReference<Session> defaultSession = defaultThreadLocalSessions.get();
        if (defaultSession != null && defaultSession.get() != null) {
            Session session = defaultSession.get();
            if(!session.isClosed()) {
                return session;
            }
        }
        return null;
    }

    /**
     * check whether session binded to current thread first, if not, use EntitySessionFactory.currentSession(),
     * and set result to binded session of current thread
     *
     * notice, the session is stored in thread local with weak reference, if not kept
     *
     * @return current session of current thread
     */
    public static Session currentSession() {
        WeakReference<Session> defaultSession = defaultThreadLocalSessions.get();
        if (defaultSession != null && defaultSession.get() != null) {
            Session session = defaultSession.get();
            if(!session.isClosed()) {
                return session;
            }
        }
        SessionFactory sessionFactory = defaultSessionFactory;
        if(sessionFactory==null) {
            sessionFactory = EntitySessionFactory.getDefaultEntitySessionFactory();
        }
        Session session = sessionFactory.createSession();
        defaultThreadLocalSessions.set(new WeakReference<Session>(session));
        return session;
    }

    public static void removeBindingCurrentSession() {
        defaultThreadLocalSessions.remove();
    }

    public static Session bindCurrentSession(Session session) {
        synchronized (defaultThreadLocalSessions) {
            if(session == null) {
                defaultThreadLocalSessions.remove();
            } else {
                defaultThreadLocalSessions.set(new WeakReference<Session>(session));
            }
            return session;
        }
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
