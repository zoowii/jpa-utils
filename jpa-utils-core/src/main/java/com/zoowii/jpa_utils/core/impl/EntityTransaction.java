package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.Transaction;

/**
 * Created by zoowii on 14-12-23.
 */
public class EntityTransaction implements Transaction {
    private final javax.persistence.EntityTransaction entityTransaction;

    public EntityTransaction(javax.persistence.EntityTransaction entityTransaction) {
        this.entityTransaction = entityTransaction;
    }

    @Override
    public void begin() {
        entityTransaction.begin();
    }

    @Override
    public void commit() {
        entityTransaction.commit();
    }

    @Override
    public void rollback() {
        entityTransaction.rollback();
    }

    @Override
    public boolean isActive() {
        return entityTransaction.isActive();
    }
}
