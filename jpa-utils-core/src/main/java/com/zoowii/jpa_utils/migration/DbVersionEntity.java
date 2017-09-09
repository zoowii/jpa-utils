package com.zoowii.jpa_utils.migration;

import com.zoowii.jpa_utils.orm.Model;
import com.zoowii.jpa_utils.query.Finder;

import javax.persistence.*;

/**
 * 数据库migration记录表
 */
@Entity
@Table(name = "db_versions")
public class DbVersionEntity extends Model {
    public static final Finder<Long, DbVersionEntity> find = new Finder<Long, DbVersionEntity>(Long.class, DbVersionEntity.class);

    private long version;

    @Id
    @Column(name = "version", unique = true)
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
