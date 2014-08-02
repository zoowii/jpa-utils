package com.zoowii.jpa_utils.query;

import com.zoowii.jpa_utils.orm.Model;

import java.util.List;

/**
 * finder class to query model
 *
 * @param <M> model class
 * @param <K> modek primary key class
 */
public class Finder<K, M extends Model> {
    protected Class modelCls;
    protected Class keyCls;

    public Finder(Class kCls, Class mCls) {
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
