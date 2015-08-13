package com.zoowii.jpa_utils.builders;

import com.alibaba.fastjson.JSON;
import com.zoowii.jpa_utils.annotations.Jsonb;
import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.util.FieldAccessor;
import org.apache.commons.lang3.reflect.MethodUtils;

import javax.persistence.Column;

/**
 * Created by zoowii on 2015/8/13.
 */
public class SqlDataBuilder {

    /**
     * some column data need special procession, eg. jsonb, hstore, etc.
     *
     * @param fieldAccessor
     * @param entity
     * @param propertyValue
     * @return
     */
    public static Object getDataForSqlWrite(FieldAccessor fieldAccessor, Object entity, Object propertyValue) {
        Column columnAnno = fieldAccessor.getPropertyAnnotation(Column.class);
        if ((columnAnno != null && !columnAnno.nullable()) && fieldAccessor.getPropertyType() == String.class && propertyValue == null) {
            return "";
        }
        if (propertyValue == null) {
            return null;
        }
        if (fieldAccessor.getPropertyAnnotation(Jsonb.class) != null) {
            try {
                Class<?> pgObjectCls = Class.forName("org.postgresql.util.PGobject");
                String jsonText = JSON.toJSONString(propertyValue);
                Object pgObject = pgObjectCls.newInstance();
                // TODO: cache this method call or direct use PGobject by injection
                MethodUtils.invokeMethod(pgObject, "setType", "text");
                MethodUtils.invokeMethod(pgObject, "setValue", jsonText);
                return pgObject;
            } catch (Exception e) {
                throw new JdbcRuntimeException(e);
            }
        }
        // may need more annotation processor
        return propertyValue;
    }

    public static Object getDataForSqlWrite(FieldAccessor fieldAccessor, Object entity) {
        Object propertyValue = fieldAccessor.getProperty(entity);
        return getDataForSqlWrite(fieldAccessor, entity, propertyValue);
    }
}
