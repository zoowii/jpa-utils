package com.zoowii.jpa_utils.core.impl;

import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import org.apache.commons.dbutils.BeanProcessor;

import java.beans.PropertyDescriptor;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
}
