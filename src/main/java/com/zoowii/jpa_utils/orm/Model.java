package com.zoowii.jpa_utils.orm;


import com.zoowii.jpa_utils.core.Session;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class Model {

    public static Session getSession() {
        return Session.currentSession();
    }

    public void beforeSave() {

    }

    public void afterSave() {

    }

    public void save() {
        beforeSave();
        getSession().save(this);
        afterSave();
    }

    public void beforeUpdate() {

    }

    public void afterUpdate() {

    }

    public void update() {
        beforeUpdate();
        getSession().update(this);
        afterUpdate();
    }

    public void beforeDelete() {

    }

    public void afterDelete() {

    }

    public void delete() {
        beforeDelete();
        getSession().delete(this);
        afterDelete();
    }

    public void beforeRefresh() {

    }

    public void afterRefresh() {

    }

    public void refresh() {
        beforeRefresh();
        getSession().refresh(this);
        afterRefresh();
    }

    public void beforeMerge() {

    }

    public void afterMerge() {

    }

    public void merge() {
        beforeMerge();
        getSession().merge(this);
        afterMerge();
    }
}
