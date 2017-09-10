package com.zoowii.jpa_utils.test.migrations;

import com.zoowii.jpa_utils.exceptions.MigrationException;
import com.zoowii.jpa_utils.migration.AbstractDbMigration;
import com.zoowii.jpa_utils.test.models.TestUser1Entity;

public class CreateUserTableFromEntityMigration extends AbstractDbMigration {
    @Override
    public void change() throws MigrationException {
        createTableFromEntity(TestUser1Entity.class);
    }

    @Override
    public long getVersion() {
        return 1505059004L;
    }
}
