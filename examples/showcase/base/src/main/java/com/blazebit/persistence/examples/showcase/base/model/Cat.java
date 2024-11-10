/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.base.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity
public class Cat {

    private Integer id;
    private String name;
    private Cat father;
    private Cat mother;
    private Set<Cat> kittens;

    public Cat() {
    }

    public Cat(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Cat getFather() {
        return father;
    }

    public void setFather(Cat father) {
        this.father = father;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Cat getMother() {
        return mother;
    }

    public void setMother(Cat mother) {
        this.mother = mother;
    }

    @OneToMany
    public Set<Cat> getKittens() {
        return kittens;
    }

    public void setKittens(Set<Cat> kittens) {
        this.kittens = kittens;
    }

}
