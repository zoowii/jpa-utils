package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zoowii on 15/5/1.
 */
public class JdbcOrmBeanHandler<T> implements ResultSetHandler<T> {
    private final Class<T> type;
    private final RowProcessor convert;
    private static final Map<Class<?>, RowProcessor> ROW_PROCESSOR_MAP = new HashMap<Class<?>, RowProcessor>();
    public static synchronized RowProcessor getRowProcessor(Class<?> modelCls, ModelMeta modelMeta) {
        if(ROW_PROCESSOR_MAP.containsKey(modelCls)) {
            return ROW_PROCESSOR_MAP.get(modelCls);
        }
        RowProcessor rowProcessor = new BasicRowProcessor(new JdbcOrmBeanProcessor(modelCls, modelMeta));
        ROW_PROCESSOR_MAP.put(modelCls, rowProcessor);
        return rowProcessor;
    }

    public JdbcOrmBeanHandler(Class<T> type, ModelMeta modelMeta) {
        this(type, getRowProcessor(type, modelMeta));
    }

    public JdbcOrmBeanHandler(Class<T> type, RowProcessor convert) {
        this.type = type;
        this.convert = convert;
    }

    public T handle(ResultSet rs) throws SQLException {
        return rs.next()?this.convert.toBean(rs, this.type):null;
    }
}
