/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.graphql.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Christian Beikov
 * @since 1.6.15
 */
@Entity
public class Pet {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private Integer age;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private Human human;

    public Pet() {
    }

    public Pet(String name, Integer age, Human human) {
        this.name = name;
        this.age = age;
        this.human = human;
    }

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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
