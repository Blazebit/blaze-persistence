/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import com.blazebit.persistence.CTE;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
@CTE
public class TestAdvancedCTE1 implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private TestCTEEmbeddable embeddable;
    private Integer level;
    private RecursiveEntity parent;
    private Long parentId;

    // overrides
    private String name;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Embedded
    public TestCTEEmbeddable getEmbeddable() {
        return embeddable;
    }

    public void setEmbeddable(TestCTEEmbeddable embeddable) {
        this.embeddable = embeddable;
    }

    @Column(name = "nesting_level")
    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    public RecursiveEntity getParent() {
        return parent;
    }
    
    public void setParent(RecursiveEntity owner) {
        this.parent = owner;
    }

    @Column(name = "parent_id", insertable = false, updatable = false)
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long ownerId) {
        this.parentId = ownerId;
    }

    @Column(name = "name", insertable = false, updatable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
