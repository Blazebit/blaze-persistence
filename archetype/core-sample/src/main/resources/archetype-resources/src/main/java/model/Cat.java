/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

@Entity
public class Cat {
    
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private Integer age;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private Person owner;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private Cat mother;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private Cat father;
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
