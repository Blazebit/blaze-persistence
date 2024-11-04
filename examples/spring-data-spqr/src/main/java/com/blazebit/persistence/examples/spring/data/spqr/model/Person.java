/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.spqr.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@Entity
public class Person {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @OneToMany(mappedBy = "owner")
    private Set<Cat> kittens = new HashSet<>();
    @OneToMany(mappedBy = "parent")
    private Set<Child> children = new HashSet<>();

    public Person() {
    }

    public Person(String name, Set<Child> children) {
        this.name = name;
        this.children = children;
        for (Child child : children) {
            child.setParent(this);
        }
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

    public Set<Cat> getKittens() {
        return kittens;
    }

    public void setKittens(Set<Cat> kittens) {
        this.kittens = kittens;
    }

    public Set<Child> getChildren() {
        return children;
    }

    public void setChildren(Set<Child> children) {
        this.children = children;
    }
}
