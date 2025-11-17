/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.webmvc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
public class Cat {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private Integer age;
    @JsonIgnoreProperties("kittens")
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private Person owner;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private Cat mother;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private Cat father;
    @JsonIgnore
    @ManyToMany
    private Set<Cat> kittens = new HashSet<>();

    public Cat() {
    }

    public Cat(String name, Integer age, Person owner) {
        this.name = name;
        this.age = age;
        this.owner = owner;
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

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public Cat getMother() {
        return mother;
    }

    public void setMother(Cat mother) {
        this.mother = mother;
    }

    public Cat getFather() {
        return father;
    }

    public void setFather(Cat father) {
        this.father = father;
    }

    public Set<Cat> getKittens() {
        return kittens;
    }

    public void setKittens(Set<Cat> kittens) {
        this.kittens = kittens;
    }
}
