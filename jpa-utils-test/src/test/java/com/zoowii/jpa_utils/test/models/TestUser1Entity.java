package com.zoowii.jpa_utils.test.models;

import com.zoowii.jpa_utils.orm.Model;
import com.zoowii.jpa_utils.query.Finder;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "test1_user", indexes = {
        @Index(name = "test1_user_name_key", columnList = "name", unique = true),
        @Index(name = "test1_user_name_age_key", columnList = "name,age", unique = false)
})
public class TestUser1Entity extends Model {
    public static final Finder<Long, TestUser1Entity> find = new Finder<Long, TestUser1Entity>(Long.class, TestUser1Entity.class);

    private Long id;

    private String name;
    private Integer age;

    private BigDecimal amount = new BigDecimal(0);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, columnDefinition = "bigint not null auto_increment")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "name", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "age")
    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Column(name = "amount", nullable = false)
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
