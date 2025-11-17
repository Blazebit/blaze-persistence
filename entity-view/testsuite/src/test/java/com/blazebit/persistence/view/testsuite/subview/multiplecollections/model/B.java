/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subview.multiplecollections.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@Entity
public class B {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToMany
    @JoinTable
    private Set<C> cSet = new HashSet<>(0);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<C> getcSet() {
        return cSet;
    }

    public void setcSet(Set<C> cSet) {
        this.cSet = cSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof B)) return false;
        B b = (B) o;
        // Null-ids are never equal
        return id != null && b.id != null && Objects.equals(id, b.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
