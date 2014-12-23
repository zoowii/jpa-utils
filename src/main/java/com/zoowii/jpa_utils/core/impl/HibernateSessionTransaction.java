package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.Transaction;

/**
 * Created by zoowii on 14-12-23.
 */
public class HibernateSessionTransaction implements Transaction {
    private final org.hibernate.Transaction hibernateTransaction;

    public HibernateSessionTransaction(org.hibernate.Transaction hibernateTransaction) {
        this.hibernateTransaction = hibernateTransaction;
    }

    @Override
    public void begin() {
        hibernateTransaction.begin();
    }

    @Override
    public void commit() {
        hibernateTransaction.commit();
    }

    @Override
    public void rollback() {
        hibernateTransaction.rollback();
    }

    public boolean wasCommitted() {
        return hibernateTransaction.wasCommitted();
    }

    @Override
    public boolean isActive() {
        return hibernateTransaction.isActive();
    }

    public boolean wasRolledBack() {
        return hibernateTransaction.wasRolledBack();
    }

    public void setTimeout(int n) {
        hibernateTransaction.setTimeout(n);
    }
}
