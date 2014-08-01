package com.zoowii.hibernate_utils.query;

import com.zoowii.hibernate_utils.orm.Model;

import java.util.List;

/**
 * finder class to query model
 *
 * @param <M> model class
 * @param <K> modek primary key class
 */
public class Finder<M extends Model, K> {
    protected Class modelCls;
    protected Class keyCls;

    public Finder(Class mCls, Class kCls) {
        modelCls = mCls;
        keyCls = kCls;
    }

    public M byId(K key) {
        return (M) M.getSession().find(modelCls, key);
    }

    public List<M> findAll() {
        return M.getSession().findListByQuery(modelCls, "from " + modelCls.getSimpleName());
    }

    public Query<M> where() {
        return new Query<M>(modelCls);
    }
}
