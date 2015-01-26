package com.zoowii.jpa_utils.jdbcorm.sqlcolumntypes;

import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.util.StringUtil;

import java.util.Date;

/**
 * 用来映射JAVA orm model的列类型到SQL数据库中的字段类型
 * Created by zoowii on 15/1/26.
 */
public abstract class SqlColumnTypeMapper {
    public abstract String getOfInteger();

    public abstract String getOfLong();

    public abstract String getOfString();

    public abstract String getOfBoolean();

    public abstract String getOfString(int length);

    public abstract String getOfText(boolean isLob);

    public abstract String getOfBytes(boolean isLob);

    public abstract String getOfDate();

    public abstract String getOfDateTime();

    public abstract String getOfTimestamp();

    public String get(Class<?> propertyCls, javax.persistence.Column columnAnno, boolean isLob) {
        if (columnAnno != null && !StringUtil.isEmpty(columnAnno.columnDefinition())) {
            return columnAnno.columnDefinition();
        }
        if (propertyCls == Integer.class) {
            return getOfInteger();
        }
        if (propertyCls == Long.class) {
            return getOfLong();
        }
        if (propertyCls == String.class) {
            if (isLob) {
                return getOfText(true);
            }
            if (columnAnno != null) {
                return getOfString(columnAnno.length());
            } else {
                return getOfString();
            }
        }
        if (propertyCls == Boolean.class) {
            return getOfBoolean();
        }
        if (propertyCls == Date.class || propertyCls == java.sql.Time.class) {
            return getOfDateTime();
        }
        if (propertyCls == java.sql.Date.class) {
            return getOfDate();
        }
        if (propertyCls == java.sql.Timestamp.class) {
            return getOfTimestamp();
        }
        if (propertyCls.isArray() && (propertyCls.getComponentType() == Byte.class || propertyCls.getComponentType().getName().equals("byte"))) {
            return getOfBytes(isLob);
        }
        throw new JdbcRuntimeException("Can't find sql column type of column type " + propertyCls.getName());
    }
}
