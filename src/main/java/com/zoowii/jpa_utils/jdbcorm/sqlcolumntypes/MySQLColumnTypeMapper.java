package com.zoowii.jpa_utils.jdbcorm.sqlcolumntypes;

/**
 * Created by zoowii on 15/1/26.
 */
public class MySQLColumnTypeMapper extends SqlColumnTypeMapper {
    @Override
    public String getOfInteger() {
        return "int(11)";
    }

    @Override
    public String getOfLong() {
        return "bigint";
    }

    @Override
    public String getOfString() {
        return "varchar(255)";
    }

    @Override
    public String getOfBoolean() {
        return "int(11)";
    }

    @Override
    public String getOfString(int length) {
        return String.format("varchar(%d)", length);
    }

    @Override
    public String getOfText(boolean isLob) {
        return isLob ? "text" : "varchar(2000)";
    }

    @Override
    public String getOfBytes(boolean isLob) {
        return "blob";
    }

    @Override
    public String getOfDate() {
        return "date";
    }

    @Override
    public String getOfDateTime() {
        return "datetime";
    }

    @Override
    public String getOfTimestamp() {
        return "timestamp";
    }
}
