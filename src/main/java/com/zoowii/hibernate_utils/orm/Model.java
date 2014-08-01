package com.zoowii.hibernate_utils.orm;


import com.alibaba.fastjson.annotation.JSONField;
import com.zoowii.hibernate_utils.core.Session;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public class Model {

    public static Session getSession() {
        return Session.currentSession();
    }

    public void save() {
        getSession().save(this);
    }

    public void update() {
        getSession().update(this);
    }

    public void delete() {
        getSession().delete(this);
    }

    public void refresh() {
        getSession().refresh(this);
    }

    public void merge() {
        getSession().merge(this);
    }
}
