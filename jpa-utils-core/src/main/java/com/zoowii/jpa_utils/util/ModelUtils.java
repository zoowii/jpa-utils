package com.zoowii.jpa_utils.util;

import com.zoowii.jpa_utils.annotations.QuerySql;
import com.zoowii.jpa_utils.annotations.QuerySqls;

import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zoowii on 14-12-23.
 */
public class ModelUtils {
    /**
     * 从model类中找到对应的数据库表名
     *
     * @param cls cls to find
     * @return table name found
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

    private static final Object modelQuerySqlsLock = new Object();
    private static Map<Class<?>, Map<String, String>> modelQueryRawSqlsMappings = new HashMap<Class<?>, Map<String, String>>();
    private static Map<Class<?>, Map<String, String>> modelQuerySqlsMappings = new HashMap<Class<?>, Map<String, String>>();

    /**
     * 从model类的@QuerySql注解中找到名称为name的原生SQL查询
     *
     * @param model
     * @param name
     * @return
     */
    public static String findRawQuerySqlByName(Class<?> model, String name) {
        Map<String, String> rawQueryMappings = modelQueryRawSqlsMappings.get(model);
        if (rawQueryMappings == null) {
            synchronized (modelQuerySqlsLock) {
                rawQueryMappings = modelQueryRawSqlsMappings.get(model);
                if (rawQueryMappings == null) {
                    QuerySqls querySqlsAnno = model.getAnnotation(QuerySqls.class);
                    if (querySqlsAnno == null) {
                        return null;
                    }
                    QuerySql[] rawQuerySqls = querySqlsAnno.rawQueries();
                    rawQueryMappings = new HashMap<String, String>();
                    for (QuerySql querySql : rawQuerySqls) {
                        rawQueryMappings.put(querySql.name(), querySql.value());
                    }
                    modelQueryRawSqlsMappings.put(model, rawQueryMappings);
                }
            }
        }
        return rawQueryMappings.get(name);
    }
}
