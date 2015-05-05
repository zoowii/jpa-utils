package com.zoowii.jpa_utils.test.models;

import com.zoowii.jpa_utils.orm.Model;
import com.zoowii.jpa_utils.query.Finder;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by zoowii on 15/1/28.
 */
@Entity
@Table(name = "jpa_user")
public class User extends Model {
    public static final Finder<Long, User> find = new Finder<Long, User>(Long.class, User.class);
    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id = UUID.randomUUID().toString();
    private String name;
    @Column(name = "test_age")
    private Integer age;
    @Column(name = "random_number")
    private int randomNumber;
    @Transient
    private String other;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public int getRandomNumber() {
        return randomNumber;
    }

    public void setRandomNumber(int randomNumber) {
        this.randomNumber = randomNumber;
    }
}
