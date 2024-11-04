/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.io.Serializable;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@NamedQueries({
    @NamedQuery(name = "findD1", query = "select d from Document d where d.name = 'D1'"),
    @NamedQuery(name = "findIdOfD1", query = "select d.id from Document d where d.name = 'D1'")
})
@Entity
public class Document implements Serializable {

    private Long id;
    private String name;
    private String description;
    private long age;
    private Person owner;
    private MyEnum status = MyEnum.ABC;

    public Document() {
    }

    public Document(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    @Enumerated(EnumType.STRING)
    public MyEnum getStatus() {
        return status;
    }

    public void setStatus(MyEnum status) {
        this.status = status;
    }
}
