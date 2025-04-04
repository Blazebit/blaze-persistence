/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Person {
    
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @OneToMany(mappedBy = "owner")
    private Set<Cat> kittens = new HashSet<>();

    public Person() {
    }

    public Person(String name) {
        this.name = name;
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
}
