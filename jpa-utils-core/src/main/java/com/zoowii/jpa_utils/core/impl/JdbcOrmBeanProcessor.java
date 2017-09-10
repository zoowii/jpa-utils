package com.zoowii.jpa_utils.core.impl;

import com.alibaba.fastjson.JSON;
import com.zoowii.jpa_utils.jdbcorm.ModelMeta;
import com.zoowii.jpa_utils.util.Logger;
import com.zoowii.jpa_utils.util.ModelUtils;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/**
 * Created by zoowii on 15/5/1.
 */
public class JdbcOrmBeanProcessor extends BeanProcessor {
    private final Class<?> modelCls;
    private final ModelMeta modelMeta;
    private final Map<String, String> columnToPropertyOverrides;
    private static final Map<Class<?>, Object> primitiveDefaults = new HashMap();

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

        for (int col = 1; col <= cols; ++col) {
            String columnName = rsmd.getColumnLabel(col);
            if (null == columnName || 0 == columnName.length()) {
                columnName = rsmd.getColumnName(col);
            }
//            ModelMeta.ModelColumnMeta modelColumnMeta = this.modelMeta.getColumnMetaBySqlColumnName(columnName);
//            String propertyName = modelColumnMeta != null ? modelColumnMeta.fieldName : null;
            String propertyName = this.columnToPropertyOverrides.get(columnName.toLowerCase());
            if (propertyName == null) {
                propertyName = columnName;
            }

            for (int i = 0; i < props.length; ++i) {
                if (propertyName.equalsIgnoreCase(props[i].getName())) {
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

    private PropertyDescriptor[] propertyDescriptors(Class<?> c) throws SQLException {
        BeanInfo beanInfo = null;

        try {
            beanInfo = Introspector.getBeanInfo(c);
        } catch (IntrospectionException var4) {
            throw new SQLException("Bean introspection failed: " + var4.getMessage());
        }

        return beanInfo.getPropertyDescriptors();
    }

    public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
        ArrayList results = new ArrayList();
        if (!rs.next()) {
            return results;
        } else {
            if (Map.class.isAssignableFrom(type)) {
                do {
                    Map record = (Map) ModelUtils.tryNewInstanceForClass(type);
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnsCount = metaData.getColumnCount();
                    for (int i = 0; i < columnsCount; ++i) {
//                        String columnName = metaData.getColumnName(i + 1);
                        String columnName = metaData.getColumnLabel(i + 1);
                        if (null == columnName || 0 == columnName.length()) {
                            columnName = metaData.getColumnName(i + 1);
                        }
                        record.put(columnName, rs.getObject(i + 1));
                    }
                    results.add(record);
                } while (rs.next());
                return results;
            }
            PropertyDescriptor[] props = this.propertyDescriptors(type);
            ResultSetMetaData rsmd = rs.getMetaData();
            int[] columnToProperty = this.mapColumnsToProperties(rsmd, props);
            do {
                results.add(this.createBean(rs, type, props, columnToProperty));
            } while (rs.next());
            return results;
        }
    }

    private <T> T createBean(ResultSet rs, Class<T> type, PropertyDescriptor[] props, int[] columnToProperty) throws SQLException {
        if(type== BigDecimal.class) {
            System.out.print("");
        }
        Object bean = this.newInstance(type);

        for (int i = 1; i < columnToProperty.length; ++i) {
            if (columnToProperty[i] != -1) {
                PropertyDescriptor prop = props[columnToProperty[i]];
                Class propType = prop.getPropertyType();
                Object value = null;
                if (propType != null) {
                    value = this.processColumn(rs, i, propType);
                    if (value == null && propType.isPrimitive()) {
                        value = primitiveDefaults.get(propType);
                    }
                }

                this.callSetter(bean, prop, value);
            }
        }

        return (T) bean;
    }

    private void callSetter(Object target, PropertyDescriptor prop, Object value) throws SQLException {
        Method setter = prop.getWriteMethod();
        if (setter != null) {
            Class[] params = setter.getParameterTypes();

            try {
                if (value instanceof java.util.Date) {
                    String e = params[0].getName();
                    if ("java.sql.Date".equals(e)) {
                        value = new java.sql.Date(((java.util.Date) value).getTime());
                    } else if ("java.sql.Time".equals(e)) {
                        value = new Time(((java.util.Date) value).getTime());
                    } else if ("java.sql.Timestamp".equals(e)) {
                        Timestamp tsValue = (Timestamp) value;
                        int nanos = tsValue.getNanos();
                        value = new Timestamp(tsValue.getTime());
                        ((Timestamp) value).setNanos(nanos);
                    }
                } else if (value instanceof String && params[0].isEnum()) {
                    value = Enum.valueOf(params[0].asSubclass(Enum.class), (String) value);
                }
                Object compatibleValue = this.isCompatibleType(value, params[0]);
                if (compatibleValue!=null) {
                    setter.invoke(target, new Object[]{compatibleValue});
                } else {
                    throw new SQLException("Cannot set " + prop.getName() + ": incompatible types, cannot convert " + value.getClass().getName() + " to " + params[0].getName());
                }
            } catch (IllegalArgumentException var9) {
                throw new SQLException("Cannot set " + prop.getName() + ": " + var9.getMessage());
            } catch (IllegalAccessException var10) {
                throw new SQLException("Cannot set " + prop.getName() + ": " + var10.getMessage());
            } catch (InvocationTargetException var11) {
                throw new SQLException("Cannot set " + prop.getName() + ": " + var11.getMessage());
            }
        }
    }

    /**
     * 如果是兼容类型,返回值本身或者转换类型后的值,否则返回null
     * @param value
     * @param type
     * @return
     */
    private Object isCompatibleType(Object value, Class<?> type) {
        if(value == null) {
            return null;
        }
        if(type.isInstance(value)) {
            return value;
        }
        if(type.equals(Integer.TYPE) && value instanceof Integer) {
            return value;
        }
        if(type.equals(Long.TYPE) && value instanceof Long) {
            return value;
        }
        if(type.equals(Double.TYPE) && value instanceof Double) {
            return value;
        }
        if(type.equals(Float.TYPE) && value instanceof Float) {
            return value;
        }
        if(type.equals(Short.TYPE) && value instanceof Short) {
            return value;
        }
        if(type.equals(Byte.TYPE) && value instanceof Byte) {
            return value;
        }
        if(type.equals(Character.TYPE) && value instanceof Character) {
            return value;
        }
        if(type.equals(Boolean.TYPE) && value instanceof Boolean) {
            return value;
        }
        if(type == BigDecimal.class) {
            if(value instanceof Integer || value instanceof Long || value instanceof Short) {
                return BigDecimal.valueOf(Long.valueOf(value.toString()));
            } else {
                return null;
            }
        }
        return null;
    }

    static {
        primitiveDefaults.put(Integer.TYPE, Integer.valueOf(0));
        primitiveDefaults.put(Short.TYPE, Short.valueOf((short) 0));
        primitiveDefaults.put(Byte.TYPE, Byte.valueOf((byte) 0));
        primitiveDefaults.put(Float.TYPE, Float.valueOf(0.0F));
        primitiveDefaults.put(Double.TYPE, Double.valueOf(0.0D));
        primitiveDefaults.put(Long.TYPE, Long.valueOf(0L));
        primitiveDefaults.put(Boolean.TYPE, Boolean.FALSE);
        primitiveDefaults.put(Character.TYPE, Character.valueOf('\u0000'));
    }
}
