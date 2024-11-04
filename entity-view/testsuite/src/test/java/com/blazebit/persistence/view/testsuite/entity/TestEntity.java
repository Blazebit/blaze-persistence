/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.entity;

import javax.persistence.Entity;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
@Entity
public class TestEntity extends NamedEntity {
    private static final long serialVersionUID = 1L;

    private String description;

    public TestEntity() {
    }

    public TestEntity(String name, String description) {
        super(name);
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
