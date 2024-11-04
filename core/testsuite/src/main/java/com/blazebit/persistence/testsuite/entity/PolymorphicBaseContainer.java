/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.util.Objects;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Entity
public class PolymorphicBaseContainer {

    private Long id;
    private PolymorphicBase owner;
    private PolymorphicBase child;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToOne(optional = false)
    public PolymorphicBase getOwner() {
        return owner;
    }

    public void setOwner(PolymorphicBase owner) {
        this.owner = owner;
    }

    @ManyToOne
    @JoinColumn(name = "child_id")
    public PolymorphicBase getChild() {
        return child;
    }

    public void setChild(PolymorphicBase child) {
        this.child = child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PolymorphicBaseContainer that = (PolymorphicBaseContainer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
