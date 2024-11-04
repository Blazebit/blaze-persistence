/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subview.multiplecollections.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@Entity
public class A {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToMany
    private Set<B> bSet = new HashSet<>(0);
    @ManyToMany
    private Set<C> cSet = new HashSet<>(0);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<B> getbSet() {
        return bSet;
    }

    public void setbSet(Set<B> bSet) {
        this.bSet = bSet;
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
        if (!(o instanceof A)) return false;
        A a = (A) o;
        // Null-ids are never equal
        return id != null && a.id != null && Objects.equals(id, a.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
