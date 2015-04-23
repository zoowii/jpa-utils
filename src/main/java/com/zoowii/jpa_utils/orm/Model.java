package com.zoowii.jpa_utils.orm;


import com.zoowii.jpa_utils.core.AbstractSession;
import com.zoowii.jpa_utils.core.impl.EntitySession;
import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.query.Expr;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class Model {

    /**
     * 默认使用JPA的方式，如果有需要，请在执行DB操作时手动传入session
     *
     * @return the result session
     */
    public static Session getSession() {
        return AbstractSession.currentSession();
    }

    public void beforeSave(Session session) {

    }

    public void afterSave(Session session) {

    }

    public void save() {
        save(getSession());
    }

    public void save(Session session) {
        beforeSave(session);
        session.save(this);
        afterSave(session);
    }

    public void beforeUpdate(Session session) {

    }

    public void afterUpdate(Session session) {

    }

    public void update() {
        update(getSession());
    }

    public void update(Session session) {
        beforeUpdate(session);
        session.update(this);
        afterUpdate(session);
    }

    public void beforeDelete(Session session) {

    }

    public void afterDelete(Session session) {

    }

    public void delete() {
        delete(getSession());
    }

    public void delete(Session session) {
        beforeDelete(session);
        session.delete(this);
        afterDelete(session);
    }

    public void beforeRefresh(Session session) {

    }

    public void afterRefresh(Session session) {

    }

    public void refresh() {
        refresh(getSession());
    }

    public void refresh(Session session) {
        beforeRefresh(session);
        session.refresh(this);
        afterRefresh(session);
    }

    public void beforeMerge(Session session) {

    }

    public void afterMerge(Session session) {

    }

    public void merge() {
        merge(getSession());
    }

    public void merge(Session session) {
        beforeMerge(session);
        session.merge(this);
        afterMerge(session);
    }
    public void beforeDetach(Session session) {

    }
    public void afterDetach(Session session) {

    }
    public void detach() {
        detach(getSession());
    }
    public void detach(Session session) {
        beforeDetach(session);
        session.detach(this);
        afterDetach(session);
    }
}
