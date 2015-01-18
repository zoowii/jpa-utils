package com.zoowii.jpa_utils.core.impl;


import com.zoowii.jpa_utils.core.AbstractSessionFactory;
import com.zoowii.jpa_utils.core.Session;

/**
 * Created by zoowii on 14-12-23.
 */
public class HibernateSessionFactory extends AbstractSessionFactory {
    private final org.hibernate.SessionFactory hibernateSessionFactory;

    public HibernateSessionFactory(org.hibernate.SessionFactory hibernateSessionFactory) {
        this.hibernateSessionFactory = hibernateSessionFactory;
    }

    @Override
    public Session createSession() {
        return new HibernateSession(this);
    }

    @Override
    public void close() {
        hibernateSessionFactory.close();
    }

    public org.hibernate.SessionFactory getHibernateOriginSessionFactory() {
        return hibernateSessionFactory;
    }
}
