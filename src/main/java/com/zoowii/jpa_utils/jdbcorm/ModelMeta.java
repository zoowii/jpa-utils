package com.zoowii.jpa_utils.jdbcorm;

import com.zoowii.jpa_utils.exceptions.JdbcRuntimeException;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.MySQLMapper;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.SqlMapper;
import com.zoowii.jpa_utils.util.FieldAccessor;
import com.zoowii.jpa_utils.util.StringUtil;
import org.apache.log4j.Logger;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * orm model类的元信息
 * Created by zoowii on 15/1/26.
 */
public class ModelMeta {
    private static final Logger LOG = Logger.getLogger(ModelMeta.class);
    private Class<?> modelCls;
    private String tableName;
    private String tableSchema;
    private Set<ModelColumnMeta> columnMetas;
    private ModelColumnMeta idColumnMeta;
    private SqlMapper sqlMapper;

    /**
     * orm model类的列信息(映射到字段),胡烈所有@Transient注解了的字段
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
            if (field.getAnnotation(Transient.class) != null) {
                continue;
            }
            ModelColumnMeta columnMeta = new ModelColumnMeta();
            columnMeta.fieldName = field.getName();
            columnMeta.fieldType = field.getType();
            if (field.getAnnotation(javax.persistence.Id.class) != null) {
                columnMeta.isId = true;
                this.idColumnMeta = columnMeta;
            }
            javax.persistence.Column columnAnno = field.getAnnotation(javax.persistence.Column.class);
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
            // 根据columnType或者@Column和@Lob注解得到SQL字段类型
            boolean isLob = field.getAnnotation(javax.persistence.Lob.class) != null;
            try {
                columnMeta.columnType = sqlMapper.get(field.getType(), columnAnno, isLob);
            } catch (JdbcRuntimeException e) {
                LOG.error(e);
                continue;
            }
            columnMetas.add(columnMeta);
        }
        return columnMetas;
    }

    public ModelMeta(Class<?> modelCls, SqlMapper sqlMapper) {
        this.sqlMapper = sqlMapper;
        // 获取orm model的元信息
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

    public ModelMeta(Class<?> modelCls) {
        this(modelCls, new MySQLMapper()); // 默认使用mysql的SQL字段类型
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
        throw new JdbcRuntimeException("Can't find column meta info of field " + fieldName + " in model " + modelCls.getName());
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
