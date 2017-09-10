package com.zoowii.jpa_utils.test.migrations;

import com.zoowii.jpa_utils.exceptions.MigrationException;
import com.zoowii.jpa_utils.migration.AbstractDbMigration;

public class AddNameKeyToUserTableMigration extends AbstractDbMigration {
    @Override
    public void change() throws MigrationException {
        addUnique("test1_user", "name", "test1_user_name_key");
    }

    @Override
    public long getVersion() {
        return 1504974486L;
    }
}
