/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.graphql.model;

import jakarta.persistence.Entity;

/**
 * @author Christian Beikov
 * @since 1.6.15
 */
@Entity
public class Dog extends Pet {

    private int barkCount;

    public Dog() {
    }

    public Dog(String name, Integer age, Human human) {
        super(name, age, human);
    }

    public int getBarkCount() {
        return barkCount;
    }

    public void setBarkCount(int barkCount) {
        this.barkCount = barkCount;
    }
}
