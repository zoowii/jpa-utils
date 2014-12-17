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
public class Finder<K, M extends Model> {
    protected Class<?> modelCls;
    protected Class<?> keyCls;

    public Finder(Class<?> kCls, Class<?> mCls) {
        modelCls = mCls;
        keyCls = kCls;
    }

    public M byId(Session session, K key) {
        if (!M.getSession().isTransactionActive()) {
            M.getSession().begin();
        }
        return (M) session.find(modelCls, key);
    }

    public M byId(K key) {
        return byId(M.getSession(), key);
    }

    public List<M> findAll(Session session) {
        if (!M.getSession().isTransactionActive()) {
            M.getSession().begin();
        }
        return session.findListByQuery(modelCls, "from " + modelCls.getSimpleName());
    }

    public List<M> findAll() {
        return findAll(M.getSession());
    }

    public Query<M> where() {
        return new Query<M>(modelCls);
    }
}
