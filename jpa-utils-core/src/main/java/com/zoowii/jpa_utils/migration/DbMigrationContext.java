package com.zoowii.jpa_utils.migration;

import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.exceptions.MigrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbMigrationContext {
    private Logger logger = LoggerFactory.getLogger(DbMigrationContext.class);

    private final Session session;

    public DbMigrationContext(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    /**
     * 检查数据库迁移记录表是否存在,如果不存在,创建此表
     */
    private void createDbMigrationVersionTableIfNotExists() {
        session.executeNativeSql("create table if not exists db_versions (version bigint not null unique)");
    }

    /**
     * 获取当前数据库的最新迁移版本(如果没有apply过migrations,返回null)
     * @return
     */
    public Long getCurrentDbMigrationVersion() {
        createDbMigrationVersionTableIfNotExists();
        DbVersionEntity dbVersionEntity = DbVersionEntity.find.where(session).orderBy("version", false).first();
        if(dbVersionEntity == null) {
            return null;
        } else {
            return dbVersionEntity.getVersion();
        }
    }

    /**
     * 更新当前数据库的最新迁移版本
     * @param newVersion
     */
    private void updateCurrentDbMigrationVersion(long newVersion) {
        createDbMigrationVersionTableIfNotExists();
        DbVersionEntity dbVersionEntity = new DbVersionEntity();
        dbVersionEntity.setVersion(newVersion);
        dbVersionEntity.save(session);
    }

    private void applyMigration(IDbMigration migration) throws MigrationException {
        try {
            migration.change();
            updateCurrentDbMigrationVersion(migration.getVersion());
            logger.info("applied migration " + migration.getClass().getCanonicalName() + " with version " + migration.getVersion());
        } catch (Exception e) {
            throw new MigrationException(e);
        }
    }

    public void loadAndApplyMigrations(List<Class<? extends AbstractDbMigration>> migrationClasses) throws MigrationException {
        List<Class<? extends IDbMigration>> commonMigrationClasses = new ArrayList<Class<? extends IDbMigration>>();
        if(migrationClasses!=null) {
            commonMigrationClasses.addAll(migrationClasses);
        }
        loadAndApplyCommonMigrations(commonMigrationClasses);
    }

    /**
     * 加载migration类型列表,对于每个migration,检查版本
     * @param migrationClasses
     */
    public void loadAndApplyCommonMigrations(List<Class<? extends IDbMigration>> migrationClasses) throws MigrationException {
        // load migration classes, check version and revolution migrations, apply newer migrations then current version one by one
        List<IDbMigration> migrations = new ArrayList<IDbMigration>();
        for(Class<? extends IDbMigration> migrationClass : migrationClasses) {
            try {
                IDbMigration migration = migrationClass.newInstance();
                migration.setContext(this);
                migrations.add(migration);
            } catch (InstantiationException e) {
                throw new MigrationException((e));
            } catch (IllegalAccessException e) {
                throw new MigrationException((e));
            }
        }
        if(migrations.isEmpty()) {
            logger.info("there are no db migrations to apply");
            return;
        }
        Collections.sort(migrations);
        for(int i=0;i<migrations.size()-1;i++) {
            if(migrations.get(i).getVersion()==migrations.get(i+1).getVersion()) {
                throw new MigrationException("error of migration version duplicate: "
                        + migrations.get(i).getClass().getCanonicalName() + " and "
                        + migrations.get(i+1).getClass().getCanonicalName());
            }
        }
        List<IDbMigration> needApplyMigrations = new ArrayList<IDbMigration>();
        Long currentDbMigrationVersion = getCurrentDbMigrationVersion();
        if(currentDbMigrationVersion==null) {
            needApplyMigrations.addAll(migrations);
        } else {
            for(IDbMigration migration : migrations) {
                if(migration.getVersion()>currentDbMigrationVersion) {
                    needApplyMigrations.add(migration);
                }
            }
        }

        if(needApplyMigrations.isEmpty()) {
            logger.info("there are no db migrations to apply");
        }

        for(IDbMigration migration : needApplyMigrations) {
            applyMigration(migration);
        }
    }
}
