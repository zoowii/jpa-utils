package com.zoowii.jpa_utils.orm;


import com.zoowii.jpa_utils.core.impl.EntitySession;
import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.query.Expr;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class Model {

    /**
     * 默认使用JPA的方式，如果有需要，请在执行DB操作时手动传入session
     *
     * @return
     */
    public static Session getSession() {
        return EntitySession.currentSession();
    }

    public void beforeSave() {

    }

    public void afterSave() {

    }

    public void save() {
        save(getSession());
    }

    public void save(Session session) {
        beforeSave();
        session.save(this);
        afterSave();
    }

    public void beforeUpdate() {

    }

    public void afterUpdate() {

    }

    public void update() {
        update(getSession());
    }

    public void update(Session session) {
        beforeUpdate();
        session.update(this);
        afterUpdate();
    }

    public void beforeDelete() {

    }

    public void afterDelete() {

    }

    public void delete() {
        delete(getSession());
    }

    public void delete(Session session) {
        beforeDelete();
        session.delete(this);
        afterDelete();
    }

    public void beforeRefresh() {

    }

    public void afterRefresh() {

    }

    public void refresh() {
        refresh(getSession());
    }

    public void refresh(Session session) {
        beforeRefresh();
        session.refresh(this);
        afterRefresh();
    }

    public void beforeMerge() {

    }

    public void afterMerge() {

    }

    public void merge() {
        merge(getSession());
    }

    public void merge(Session session) {
        beforeMerge();
        session.merge(this);
        afterMerge();
    }
}
