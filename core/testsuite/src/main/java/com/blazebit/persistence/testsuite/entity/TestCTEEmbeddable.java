/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Embeddable
public class TestCTEEmbeddable {

    private String name;
    private String description;
    private RecursiveEntity recursiveEntity;

    public TestCTEEmbeddable() {
    }

    public TestCTEEmbeddable(String name) {
        this.name = name;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recursive_entity_id")
    public RecursiveEntity getRecursiveEntity() {
        return recursiveEntity;
    }

    public void setRecursiveEntity(RecursiveEntity recursiveEntity) {
        this.recursiveEntity = recursiveEntity;
    }
}
