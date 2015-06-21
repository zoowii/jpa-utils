package com.zoowii.jpa_utils.jdbcorm;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.MySQLMapper;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
import com.zoowii.jpa_utils.util.FieldAccessor;
import com.zoowii.jpa_utils.util.Logger;
import com.zoowii.jpa_utils.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.*;

/**
 * meta info of orm model
 * Created by zoowii on 15/1/26.
 */
public class ModelMeta {
    private Class<?> modelCls;
    private String tableName;
    private String tableSchema;
    private Set<ModelColumnMeta> columnMetas;
    private ModelColumnMeta idColumnMeta;
    private SqlMapper sqlMapper;

    /**
     * column info of orm model class, ignore all fields with @javax.sql.Transient
     */
    public static class ModelColumnMeta {
        public boolean isId = false;
        public String fieldName;
        public String columnName;
        public Class<?> fieldType;
        public String columnType;
        public boolean nullable;
    }

    private Set<ModelColumnMeta> getColumnMetas() {
        Field[] fields = modelCls.getDeclaredFields();
        Set<ModelColumnMeta> columnMetas = new HashSet<ModelColumnMeta>();
        for (Field field : fields) {
            FieldAccessor fieldAccessor = new FieldAccessor(modelCls, field.getName());
            if (fieldAccessor.getPropertyAnnotation(Transient.class) != null) {
                continue;
            }
            ModelColumnMeta columnMeta = new ModelColumnMeta();
            columnMeta.fieldName = field.getName();
            columnMeta.fieldType = field.getType();
            if (fieldAccessor.getPropertyAnnotation(javax.persistence.Id.class) != null) {
                columnMeta.isId = true;
                this.idColumnMeta = columnMeta;
            }
            javax.persistence.Column columnAnno = fieldAccessor.getPropertyAnnotation(javax.persistence.Column.class);
            if (columnAnno == null) {
                columnMeta.columnName = StringUtil.underscoreName(field.getName());
                columnMeta.nullable = true;
            } else {
                columnMeta.nullable = columnAnno.nullable();
                if (StringUtil.isEmpty(columnAnno.name())) {
                    columnMeta.columnName = StringUtil.underscoreName(field.getName());
                } else {
                    columnMeta.columnName = columnAnno.name();
                }
            }
            // get sql column type from columnType or @Column or @Lob annotation
            boolean isLob = fieldAccessor.getPropertyAnnotation(javax.persistence.Lob.class) != null;
            try {
                columnMeta.columnType = sqlMapper.get(field.getType(), columnAnno, isLob);
            } catch (JdbcRuntimeException e) {
                Logger.debug("get sql model field type error", e);
                continue;
            }
            columnMetas.add(columnMeta);
        }
        return columnMetas;
    }

    private static final Map<Class<?>, ModelMeta> modelMetaCache = new HashMap<Class<?>, ModelMeta>();

    public static ModelMeta getModelMeta(Class<?> modelCls, SqlMapper sqlMapper) {
        ModelMeta modelMeta = modelMetaCache.get(modelCls);
        if(modelMeta==null) {
            synchronized (modelMetaCache) {
                if(modelMetaCache.get(modelCls)==null) {
                    modelMetaCache.put(modelCls, new ModelMeta(modelCls, sqlMapper));
                }
            }
            modelMeta = modelMetaCache.get(modelCls);
        }
        return modelMeta;
    }

    private ModelMeta(Class<?> modelCls, SqlMapper sqlMapper) {
        this.sqlMapper = sqlMapper;
        // get meta info of orm model
        this.modelCls = modelCls;
        javax.persistence.Table table = modelCls.getAnnotation(javax.persistence.Table.class);
        tableName = StringUtil.underscoreName(modelCls.getSimpleName());
        tableSchema = "";
        if (table != null) {
            if (!StringUtil.isEmpty(table.name())) {
                tableName = table.name();
            }
            tableSchema = table.schema();
        }
        columnMetas = getColumnMetas();
    }

    public Class<?> getModelCls() {
        return modelCls;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public Set<ModelColumnMeta> getColumnMetaSet() {
        return columnMetas;
    }

    public Iterator<ModelColumnMeta> iterateColumnMetas() {
        return columnMetas.iterator();
    }

    public ModelColumnMeta getIdColumnMeta() {
        return idColumnMeta;
    }

    public ModelColumnMeta getColumnMetaByFieldName(String fieldName) {
        for (ModelColumnMeta modelColumnMeta : getColumnMetaSet()) {
            if (modelColumnMeta.fieldName.equals(fieldName)) {
                return modelColumnMeta;
            }
        }
        return null;
    }

    public ModelColumnMeta getColumnMetaBySqlColumnName(String columnName) {
        for (ModelColumnMeta modelColumnMeta : getColumnMetaSet()) {
            if (modelColumnMeta.columnName.equalsIgnoreCase(columnName)) {
                return modelColumnMeta;
            }
        }
        return null;
    }

    public Map<String, String> getColumnToPropertyOverrides() {
        Map<String, String> overrides = new HashMap<String, String>();
        for (ModelColumnMeta modelColumnMeta : getColumnMetaSet()) {
            overrides.put(modelColumnMeta.columnName.toLowerCase(), modelColumnMeta.fieldName);
        }
        return overrides;
    }

    public SqlMapper getSqlMapper() {
        return sqlMapper;
    }

    public FieldAccessor getIdAccessor() {
        if (idColumnMeta == null) {
            return null;
        }
        return new FieldAccessor(modelCls, idColumnMeta.fieldName);
    }
}
