package com.zoowii.jpa_utils.core.impl;

import com.alibaba.fastjson.JSON;
import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.util.Logger;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.beans.PropertyDescriptor;
import java.sql.*;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by zoowii on 15/5/1.
 */
public class JdbcOrmBeanProcessor extends BeanProcessor {
    private final Class<?> modelCls;
    private final ModelMeta modelMeta;
    private final Map<String, String> columnToPropertyOverrides;
    public JdbcOrmBeanProcessor(Class<?> modelCls, ModelMeta modelMeta) {
        this.modelCls = modelCls;
        this.modelMeta = modelMeta;
        this.columnToPropertyOverrides = this.modelMeta.getColumnToPropertyOverrides();
    }

    @Override
    protected int[] mapColumnsToProperties(ResultSetMetaData rsmd, PropertyDescriptor[] props) throws SQLException {
        int cols = rsmd.getColumnCount();
        int[] columnToProperty = new int[cols + 1];
        Arrays.fill(columnToProperty, -1);

        for(int col = 1; col <= cols; ++col) {
            String columnName = rsmd.getColumnLabel(col);
            if(null == columnName || 0 == columnName.length()) {
                columnName = rsmd.getColumnName(col);
            }
//            ModelMeta.ModelColumnMeta modelColumnMeta = this.modelMeta.getColumnMetaBySqlColumnName(columnName);
//            String propertyName = modelColumnMeta != null ? modelColumnMeta.fieldName : null;
            String propertyName = this.columnToPropertyOverrides.get(columnName.toLowerCase());
            if(propertyName == null) {
                propertyName = columnName;
            }

            for(int i = 0; i < props.length; ++i) {
                if(propertyName.equalsIgnoreCase(props[i].getName())) {
                    columnToProperty[col] = i;
                    break;
                }
            }
        }

        return columnToProperty;
    }

    @Override
    protected Object processColumn(ResultSet rs, int index, Class<?> propType) throws SQLException {
        if (!propType.isPrimitive() && rs.getObject(index) == null) {
            return null;
        }
        if (propType.equals(String.class)) {
            return rs.getString(index);
        }
        if (propType.equals(Integer.TYPE) || propType.equals(Integer.class)) {
            return Integer.valueOf(rs.getInt(index));
        }
        if (propType.equals(Boolean.TYPE) || propType.equals(Boolean.class)) {
            return Boolean.valueOf(rs.getBoolean(index));
        }
        if (propType.equals(Long.TYPE) || propType.equals(Long.class)) {
            return Long.valueOf(rs.getLong(index));
        }
        if (propType.equals(Double.TYPE) || propType.equals(Double.class)) {
            return Double.valueOf(rs.getDouble(index));
        }
        if (propType.equals(Float.TYPE) || propType.equals(Float.class)) {
            return Float.valueOf(rs.getFloat(index));
        }
        if (propType.equals(Short.TYPE) || propType.equals(Short.class)) {
            return Short.valueOf(rs.getShort(index));
        }
        if (propType.equals(Byte.TYPE) || propType.equals(Byte.class)) {
            return Byte.valueOf(rs.getByte(index));
        }
        if (propType.equals(Timestamp.class)) {
            return rs.getTimestamp(index);
        }
        if (propType.equals(SQLXML.class)) {
            return rs.getSQLXML(index);
        }
        Object value = rs.getObject(index);
        // FIXME: abstract this processor by injection
        if (value.getClass().getCanonicalName().equals("org.postgresql.util.PGobject")) {
            try {
                String valueText = (String) MethodUtils.invokeMethod(value, "getValue");
                return JSON.toJavaObject(JSON.parseObject(valueText), propType);
            } catch (Exception e) {
                Logger.debug("getValue of PGobject instance error", e);
                return value;
            }
        } else {
            return value;
        }
    }
}
