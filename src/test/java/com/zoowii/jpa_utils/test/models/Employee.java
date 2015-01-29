package com.zoowii.jpa_utils.test.models;

import com.zoowii.jpa_utils.orm.Model;
import com.zoowii.jpa_utils.query.Finder;
import com.zoowii.jpa_utils.util.StringUtil;

import javax.persistence.*;

@Entity
@Table(name = "employee")
public class Employee extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id; // = StringUtil.randomString(30);

    @Column(nullable = false)
    private String name;

    private int age;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public static Finder<String, Employee> find = new Finder<String, Employee>(String.class, Employee.class);
}
