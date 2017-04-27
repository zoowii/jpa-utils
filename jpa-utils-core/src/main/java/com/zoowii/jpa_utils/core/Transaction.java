package com.zoowii.jpa_utils.core;

/**
 * Created by zoowii on 14-12-23.
 */
public interface Transaction {
    void begin();

    void commit();

    void rollback();

    boolean isActive();
}
