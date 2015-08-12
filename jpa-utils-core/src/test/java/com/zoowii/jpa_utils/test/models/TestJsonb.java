package com.zoowii.jpa_utils.test.models;

import com.zoowii.jpa_utils.annotations.Jsonb;
import com.zoowii.jpa_utils.annotations.NotNull;
import com.zoowii.jpa_utils.extension.ExtendFinder;
import com.zoowii.jpa_utils.orm.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Map;

/**
 * Created by zoowii on 2015/8/13.
 */
@Entity
@Table(name = "test_jsonb")
public class TestJsonb extends Model {
    public static final ExtendFinder<String, TestJsonb> find = new ExtendFinder<String, TestJsonb>(String.class, TestJsonb.class);

    private String id;
    private String name;
    private Map<String, Object> tags;

    @Id
    @Column(name = "id", nullable = false)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NotNull
    @Column(name = "name", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "tags", nullable = false)
    @Jsonb
    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }
}
