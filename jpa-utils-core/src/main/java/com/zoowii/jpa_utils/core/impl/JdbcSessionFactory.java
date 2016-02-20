package com.zoowii.jpa_utils.core.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.zoowii.jpa_utils.core.AbstractSession;
import com.zoowii.jpa_utils.core.AbstractSessionFactory;
import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.MySQLMapper;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
import com.zoowii.jpa_utils.util.StringUtil;
import com.zoowii.jpa_utils.util.cache.CacheManagerBuilder;
import com.zoowii.jpa_utils.util.cache.ICache;
import com.zoowii.jpa_utils.util.cache.ICacheManager;
import com.zoowii.jpa_utils.util.cache.MemoryCacheManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 直接使用jdbc Connection作为Session基础的session factory
 * Created by zoowii on 15/1/26.
 */
public class JdbcSessionFactory extends AbstractSessionFactory {

    private static final String CACHE_NAME = "jpa_utils_cache";

    private transient LoadingCache<Class<?>, ICache<Object, Object>> beanCacheBuilder = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<Class<?>, ICache<Object, Object>>() {
                public ICache<Object, Object> load(Class<?> key) {
                    return getCacheManager().getCache(CACHE_NAME);
                }
            });

    public interface JdbcConnectionSource {
        Connection get();
    }

    private JdbcConnectionSource jdbcConnectionSource;
    private DataSource dataSource;
    private SqlMapper sqlMapper;

    private String cacheManagerClassName;

    public String getCacheManagerClassName() {
        return cacheManagerClassName;
    }

    public void setCacheManagerClassName(String cacheManagerClassName) {
        this.cacheManagerClassName = cacheManagerClassName;
    }

    private ICacheManager cacheManager;

    public synchronized void setCacheManager(ICacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public synchronized ICacheManager getCacheManager() {
        if(cacheManager!=null) {
            return cacheManager;
        }
        if(StringUtil.isEmpty(cacheManagerClassName)) {
            cacheManager = new MemoryCacheManager();
        } else {
            try {
                cacheManager = CacheManagerBuilder.createCacheManagerBuilder(cacheManagerClassName);
                if(cacheManager == null) {
                    cacheManager = new MemoryCacheManager();
                }
            } catch (Exception e) {
                cacheManager = new MemoryCacheManager();
            }
        }
        return cacheManager;
    }

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

    @Override
    public void startCache() {
        endCache();
    }

    @Override
    public void endCache() {
        beanCacheBuilder.invalidateAll();
    }

    private String makeCacheKey(Class<?> beanCls, Object key) {
        if(beanCls == null || key == null) {
            return null;
        } else {
            return beanCls.getName() + "@@" + key;
        }
    }

    @Override
    public void cacheBean(Object key, Class<?> beanCls, Object bean) {
        if (key != null && beanCls != null && bean != null) {
            try {
                beanCacheBuilder.get(beanCls).put(makeCacheKey(beanCls, key), bean);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public <T> T getCachedBean(Object key, Class<? extends T> cls) {
        if (cls == null || key == null) {
            return null;
        }
        try {
            return (T) beanCacheBuilder.get(cls).get(makeCacheKey(cls, key));
        } catch (ExecutionException e) {
            return null;
        }
    }

    @Override
    public void removeBeanCache(Object key, Class<?> beanCls) {
        if(key!=null && beanCls!=null) {
            try {
                beanCacheBuilder.get(beanCls).remove(makeCacheKey(beanCls, key));
            } catch (ExecutionException e) {

            }
        }
    }
}
