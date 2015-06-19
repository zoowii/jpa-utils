package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.ArrayHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zoowii on 15/5/1.
 */
public class JdbcOrmBeanListHandler<T> implements ResultSetHandler<List<T>> {
    private final Class<T> type;
    private final RowProcessor convert;

    public JdbcOrmBeanListHandler(Class<T> type, ModelMeta modelMeta) {
        this(type, JdbcOrmBeanHandler.getRowProcessor(type, modelMeta));
    }

    public JdbcOrmBeanListHandler(Class<T> type, RowProcessor convert) {
        this.type = type;
        this.convert = convert;
    }

    public List<T> handle(ResultSet rs) throws SQLException {
        if(JdbcOrmBeanHandler.isRawType(type)) {
            List<T> result = new ArrayList<T>();
            while(rs.next()) {
                result.add((T)JdbcOrmBeanHandler.getResultSetRawOfRawType(rs, type));
            }
            return result;
        }
        return this.convert.toBeanList(rs, this.type);
    }
}
