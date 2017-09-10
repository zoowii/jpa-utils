package com.zoowii.jpa_utils.test.migrations;

import com.zoowii.jpa_utils.exceptions.MigrationException;
import com.zoowii.jpa_utils.migration.AbstractDbMigration;

import java.util.Arrays;

public class CreateUserTableMigration extends AbstractDbMigration {
    @Override
    public long getVersion() {
        return 1504974365L;
    }

    @Override
    public void change() throws MigrationException {
        createTable("test1_user", Arrays.asList(
                "id bigint not null AUTO_INCREMENT",
                "name varchar(50) not null"
        ));
    }
}
