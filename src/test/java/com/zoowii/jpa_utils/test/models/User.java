package com.zoowii.jpa_utils.test.models;

import com.zoowii.jpa_utils.orm.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by zoowii on 15/1/28.
 */
@Entity
@Table(name = "jpa_user")
public class User extends Model {
    @Id
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
