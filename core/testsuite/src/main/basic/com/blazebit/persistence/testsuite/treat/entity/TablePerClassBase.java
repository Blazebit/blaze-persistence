/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.treat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "table_per_class_base")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class TablePerClassBase implements Serializable, Base<TablePerClassBase, TablePerClassEmbeddable> {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Integer value;
    private TablePerClassBase parent;
    private TablePerClassEmbeddable embeddable = new TablePerClassEmbeddable();
    private Set<TablePerClassBase> children = new HashSet<>();

    public TablePerClassBase() {
    }

    public TablePerClassBase(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Id
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    @Column(name = "val")
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public TablePerClassBase getParent() {
        return parent;
    }

    @Override
    public void setParent(TablePerClassBase parent) {
        this.parent = parent;
    }

    @Override
    @Embedded
    public TablePerClassEmbeddable getEmbeddable() {
        return embeddable;
    }

    @Override
    public void setEmbeddable(TablePerClassEmbeddable embeddable) {
        this.embeddable = embeddable;
    }

    @Override
    @OneToMany(mappedBy = "parent")
    public Set<TablePerClassBase> getChildren() {
        return children;
    }

    @Override
    public void setChildren(Set<? extends TablePerClassBase> children) {
        this.children = (Set<TablePerClassBase>) children;
    }
}
