package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.AbstractSession;
import com.zoowii.jpa_utils.core.AbstractSessionFactory;
import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 直接使用jdbc Connection作为Session基础的session factory
 * Created by zoowii on 15/1/26.
 */
public class JdbcSessionFactory extends AbstractSessionFactory {

    public interface JdbcConnectionSource {
        Connection get();
    }

    private JdbcConnectionSource jdbcConnectionSource;
    private DataSource dataSource;

    public JdbcSessionFactory(JdbcConnectionSource jdbcConnectionSource) {
        this.jdbcConnectionSource = jdbcConnectionSource;
        AbstractSession.setDefaultSessionFactoryIfEmpty(this);
    }

    public JdbcSessionFactory(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcConnectionSource = new JdbcConnectionSource() {
            @Override
            public Connection get() {
                try {
                    return dataSource.getConnection();
                } catch (SQLException e) {
                    throw new JdbcRuntimeException(e);
                }
            }
        };
        AbstractSession.setDefaultSessionFactoryIfEmpty(this);
    }

    @Override
    public Session createSession() {
        return new JdbcSession(this);
    }

    @Override
    public void close() {

    }

    public java.sql.Connection createJdbcConnection() {
        return jdbcConnectionSource.get();
    }
}
