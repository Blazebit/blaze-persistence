/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jackson.testsuite.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private long age;
    private Set<Document> documents = new HashSet<>(0);

    public Person() {
        id = UUID.randomUUID().toString();
    }

    public Person(String name) {
        this();
        this.name = name;
    }

    public Person(String name, long age) {
        this(name);
        this.age = age;
    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic(optional = false)
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

    @OneToMany(mappedBy = "owner")
    public Set<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<Document> documents) {
        this.documents = documents;
    }
}
