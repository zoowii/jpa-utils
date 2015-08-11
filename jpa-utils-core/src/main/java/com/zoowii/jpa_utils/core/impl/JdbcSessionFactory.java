package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.core.AbstractSession;
import com.zoowii.jpa_utils.core.AbstractSessionFactory;
import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.MySQLMapper;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;

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
    private SqlMapper sqlMapper;

    public JdbcSessionFactory(JdbcConnectionSource jdbcConnectionSource, SqlMapper sqlMapper) {
        this.jdbcConnectionSource = jdbcConnectionSource;
        this.sqlMapper = sqlMapper;
        AbstractSession.setDefaultSessionFactoryIfEmpty(this);
    }

    public JdbcSessionFactory(JdbcConnectionSource jdbcConnectionSource) {
        this(jdbcConnectionSource, new MySQLMapper());
    }

    public JdbcSessionFactory(final DataSource dataSource, SqlMapper sqlMapper) {
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
        this.sqlMapper = sqlMapper;
        AbstractSession.setDefaultSessionFactoryIfEmpty(this);
    }

    public JdbcSessionFactory(final DataSource dataSource) {
        this(dataSource, new MySQLMapper());
    }

    public SqlMapper getSqlMapper() {
        return sqlMapper;
    }

    public void setSqlMapper(SqlMapper sqlMapper) {
        this.sqlMapper = sqlMapper;
    }

    @Override
    public Session createSession() {
        JdbcSession session = new JdbcSession(this);
        session.setSqlMapper(sqlMapper);
        return session;
    }

    @Override
    public void close() {

    }

    public java.sql.Connection createJdbcConnection() {
        return jdbcConnectionSource.get();
    }
}
