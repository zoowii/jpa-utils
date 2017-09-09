package com.zoowii.jpa_utils.test.migrations;

import com.zoowii.jpa_utils.exceptions.MigrationException;
import com.zoowii.jpa_utils.migration.AbstractDbMigration;

public class AddAgeToUserTableMigration extends AbstractDbMigration {
    @Override
    public long getVersion() {
        return 1504974386;
    }

    @Override
    public void change() throws MigrationException {
        addPrimaryKey("test1_user", "id");
        addColumn("test1_user", "age", "int(11) null");
    }
}
