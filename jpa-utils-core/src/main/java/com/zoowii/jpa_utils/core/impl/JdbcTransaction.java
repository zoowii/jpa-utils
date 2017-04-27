package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.Transaction;
import com.zoowii.jpa_utils.exceptions.JdbcTransactionRuntimeException;

/**
 * Created by zoowii on 15/1/26.
 */
public class JdbcTransaction implements Transaction {
    private final JdbcSession jdbcSession;


    public JdbcTransaction(JdbcSession jdbcSession) {
        this.jdbcSession = jdbcSession;
    }

    public boolean isAutoCommit() {
        return jdbcSession.getAutoCommit();
    }

    @Override
    public void begin() {
        if (isAutoCommit()) {
            return;
        }
        jdbcSession.getActiveFlag().set(true);
    }

    @Override
    public void commit() {
        if (isAutoCommit()) {
            return;
        }
        try {
            jdbcSession.getActiveFlag().set(true);
            jdbcSession.getJdbcConnection().commit();
            jdbcSession.getActiveFlag().set(false);
        } catch (Exception e) {
            throw new JdbcTransactionRuntimeException(e);
        }
    }

    @Override
    public void rollback() {
        if (isAutoCommit()) {
            return;
        }
        if (!jdbcSession.getActiveFlag().get()) {
            return;
        }
        try {
            jdbcSession.getActiveFlag().set(false);
            jdbcSession.getJdbcConnection().rollback();
        } catch (Exception e) {
            throw new JdbcTransactionRuntimeException(e);
        }
    }

    @Override
    public boolean isActive() {
        return jdbcSession.getActiveFlag().get();
    }
}
