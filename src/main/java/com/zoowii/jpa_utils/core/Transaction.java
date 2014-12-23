package com.zoowii.jpa_utils.core;

/**
 * Created by zoowii on 14-12-23.
 */
public interface Transaction {
    public void begin();

    public void commit();

    public void rollback();

    public boolean isActive();
}
