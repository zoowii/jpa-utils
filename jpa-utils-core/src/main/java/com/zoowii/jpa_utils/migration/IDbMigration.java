package com.zoowii.jpa_utils.migration;

import com.zoowii.jpa_utils.exceptions.MigrationException;

public interface IDbMigration extends Comparable<IDbMigration> {
    /**
     * migrate db schema from older version to newer version
     * @throws MigrationException
     */
    void change() throws MigrationException;

    void setContext(DbMigrationContext context);

    /**
     * get migration version(newer migration class must have larger version number)
     * @return
     */
    long getVersion();
}
