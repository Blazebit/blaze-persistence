/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import com.blazebit.persistence.CTE;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
@Entity
@CTE
public class PersonCTE implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private long age;
    private int idx;
    private Person owner;

    @Id
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

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public int getIdx() {
        return idx;
    }
    
    public void setIdx(int idx) {
        this.idx = idx;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    public Person getOwner() {
        return owner;
    }
    
    public void setOwner(Person owner) {
        this.owner = owner;
    }
}
