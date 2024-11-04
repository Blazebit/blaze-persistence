/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
@Entity
@IdClass(IdClassEntityId.class)
public class IdClassEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private long age;

    public IdClassEntity() {
    }

    public IdClassEntity(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

}
