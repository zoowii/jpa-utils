package com.zoowii.jpa_utils.test.migrations;

import com.zoowii.jpa_utils.exceptions.MigrationException;
import com.zoowii.jpa_utils.migration.AbstractDbMigration;

public class AddAmountToUserTableMigration extends AbstractDbMigration {
    @Override
    public void change() throws MigrationException {
        addColumn("test1_user", "amount", "bigint not null default 0");
        createOrReplaceView("test1_user_view", "select id, amount from test1_user");
    }

    @Override
    public long getVersion() {
        return 1505029796L;
    }
}
