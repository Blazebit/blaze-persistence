/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.examples.quarkus.base.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Entity
public class DocumentType {
    private String id;
    private String name;

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
