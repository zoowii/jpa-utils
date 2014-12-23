package com.zoowii.jpa_utils.util;

import javax.persistence.Table;

/**
 * Created by zoowii on 14-12-23.
 */
public class ModelUtils {
    /**
     * 从model类中找到对应的数据库表名
     *
     * @param cls
     * @return
     */
    public static String getTableNameFromModelClass(Class<?> cls) {
        if (cls == null) {
            return null;
        }
        Table table = cls.getAnnotation(Table.class);
        if (table == null || table.name() == null || table.name().trim().length() < 1) {
            return cls.getSimpleName();
        }
        return table.name().trim();
    }
}
