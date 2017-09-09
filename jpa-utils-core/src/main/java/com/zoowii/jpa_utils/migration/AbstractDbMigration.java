package com.zoowii.jpa_utils.migration;

import com.zoowii.jpa_utils.exceptions.MigrationException;
import com.zoowii.jpa_utils.util.ListUtil;
import com.zoowii.jpa_utils.util.StringUtil;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractDbMigration implements IDbMigration {

    protected DbMigrationContext context;

    public DbMigrationContext getContext() {
        return context;
    }

    @Override
    public void setContext(DbMigrationContext context) {
        this.context = context;
    }

    protected final void throwMigrationException(String message) throws MigrationException {
        throw new MigrationException(message + "(" + this.getClass().getCanonicalName() + ")");
    }

    protected void executeSql(String sql) throws MigrationException {
        if(StringUtil.isEmpty(sql)) {
            throwMigrationException("sql can't be empty");
        }
        context.getSession().executeNativeSql(sql);
    }

    /**
     * 创建数据库表,用法类似 createTable("user", Arrays.asList("name varchar(50) not null default '', age bigint, `key` varchar(50)"))
     * @param tableName
     * @param columnDefinitions
     * @throws MigrationException
     */
    protected void createTable(String tableName, List<String> columnDefinitions) throws MigrationException {
        if(StringUtil.isEmpty(tableName)) {
            throwMigrationException("table name can't be empty");
        }
        if(columnDefinitions==null || columnDefinitions.size()<1) {
            throwMigrationException("A table must have at least 1 column");
        }
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("create table ");
        sqlBuilder.append(tableName);
        sqlBuilder.append(" (");
        sqlBuilder.append(StringUtil.join(columnDefinitions, ", "));
        sqlBuilder.append(")");

        String sql = sqlBuilder.toString();
        executeSql(sql);
    }

    protected void renameTable(String oldTableName, String newTableName) throws MigrationException {
        String sql = "alter table " + oldTableName + " rename " + newTableName;
        executeSql(sql);
    }

    protected void addColumn(String tableName, String columnName, String columnDefinition) throws MigrationException {
        String sql = "alter table " + tableName + " add column " + columnName + " " + columnDefinition;
        executeSql(sql);
    }

    protected void changeColumn(String tableName, String oldColumnName, String newColumnName, String columnDefinition) throws MigrationException {
        String sql = "alter table " + tableName + " change " + oldColumnName + " " + newColumnName + " " + columnDefinition;
        executeSql(sql);
    }

    protected void removeColumn(String tableName, String columnName) throws MigrationException {
        String sql = "alter table " + tableName + " drop column " + columnName;
        executeSql(sql);
    }

    protected void addIndex(String tableName, String columnName, String indexName) throws MigrationException {
        addIndex(tableName, Arrays.asList(columnName), indexName);
    }

    protected void addIndex(String tableName, List<String> columnNames, String indexName) throws MigrationException {
        if(StringUtil.isEmpty(indexName)) {
            throwMigrationException("index name can't be empty");
        }
        if(columnNames == null || columnNames.size()<1) {
            throwMigrationException("index columns can't be empty");
        }
        String columns = StringUtil.join(columnNames, ", ");
        String sql = "create index "+indexName+" on "+tableName+" (" + columns + ")";
        executeSql(sql);
    }

    protected void addUnique(String tableName, String columnName, String indexName) throws MigrationException {
        addUnique(tableName, Arrays.asList(columnName), indexName);
    }

    protected void addUnique(String tableName, List<String> columnNames, String indexName) throws MigrationException {
        if(StringUtil.isEmpty(indexName)) {
            throwMigrationException("unique index name can't be empty");
        }
        if(columnNames == null || columnNames.size()<1) {
            throwMigrationException("unique index columns can't be empty");
        }
        String columns = StringUtil.join(columnNames, ", ");
        String sql = "create unique index "+indexName+" on "+tableName+" (" + columns + ")";
        executeSql(sql);
    }

    protected void addPrimaryKey(String tableName, String columnName) throws MigrationException {
        addPrimaryKey(tableName, Arrays.asList(columnName));
    }

    protected void addPrimaryKey(String tableName, List<String> columnNames) throws MigrationException {
        if(columnNames == null || columnNames.size()<1) {
            throwMigrationException("primary key columns can't be empty");
        }
        String columns = StringUtil.join(columnNames, ", ");
        String sql = "alter table "+tableName+" add primary key (" + columns + ")";
        executeSql(sql);
    }

    protected void dropPrimaryKey(String tableName) throws MigrationException {
        String sql = "alter table " + tableName + " drop primary key";
        executeSql(sql);
    }

    protected void dropIndex(String tableName, String indexName) throws MigrationException {
        String sql = "drop index "+indexName +" on " + tableName;
        executeSql(sql);
    }

    @Override
    public int compareTo(IDbMigration o) {
        if(o==null) {
            return -1;
        }
        long delta = this.getVersion() - o.getVersion();
        if(delta == 0) {
            return 0;
        } else if(delta<0) {
            return -1;
        } else {
            return 1;
        }
    }
}
