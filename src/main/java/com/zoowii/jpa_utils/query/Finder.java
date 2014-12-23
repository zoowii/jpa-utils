package com.zoowii.jpa_utils.query;

import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.core.impl.EntitySession;
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
        if (!Model.getSession().isTransactionActive()) {
            Model.getSession().begin();
        }
        return (M) session.find(modelCls, key);
    }

    public M byId(K key) {
        return byId(Model.getSession(), key);
    }

    public List<M> findAll(Session session) {
        if (!Model.getSession().isTransactionActive()) {
            Model.getSession().begin();
        }
        return session.findListByQuery(modelCls, "from " + modelCls.getSimpleName());
    }

    public List<M> findAll() {
        return findAll(Model.getSession());
    }

    public Query<M> where() {
        return new Query<M>(modelCls);
    }
}
