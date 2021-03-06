package com.zoowii.jpa_utils.query;

import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.orm.Model;

import java.util.List;

/**
 * finder class to query model
 *
 * @param <M> model class
 * @param <K> modek primary key class
 */
public class Finder<K, M> {
    protected Class<?> modelCls;
    protected Class<?> keyCls;

    public Finder(Class<?> kCls, Class<?> mCls) {
        modelCls = mCls;
        keyCls = kCls;
    }

    public M byId(Session session, K key) {
        if (!session.isTransactionActive()) {
            session.begin();
        }
        return (M) session.find(modelCls, key);
    }

    public M byId(K key) {
        return byId(Model.getSession(), key);
    }

    public List<M> findAll(Session session) {
        if (!session.isTransactionActive()) {
            session.begin();
        }
        return where(session).all();
//        return session.findListByQuery(modelCls, "from " + modelCls.getSimpleName());
    }

    public List<M> findAll() {
        return findAll(Model.getSession());
    }

    public Query<M> where() {
        return new Query<M>(modelCls, Model.getSession());
    }

    public long count(Session session) {
        return where(session).count();
    }

    public long count() {
        return where().count();
    }

    public Query<M> where(Session session) {
        return new Query<M>(modelCls, session);
    }
}
