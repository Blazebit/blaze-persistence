/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity
public class DocumentWithNullableName extends Ownable implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private Person friend;
    private List<Person> people = new ArrayList<Person>();

    public DocumentWithNullableName() {
    }

    public DocumentWithNullableName(Long id) {
        super(id);
    }

    public DocumentWithNullableName(String name) {
        this.name = name;
    }

    public DocumentWithNullableName(String name, Person owner) {
        this(name);
        this.setOwner(owner);
        this.setFriend(owner);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id")
    public Person getFriend() {
        return friend;
    }

    public void setFriend(Person friend) {
        this.friend = friend;
    }

    @OneToMany
    @OrderColumn(name = "people_idx", nullable = false)
    @JoinTable(name = "documentnullablename_people")
    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }
}
