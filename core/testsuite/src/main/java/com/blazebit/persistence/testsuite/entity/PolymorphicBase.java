/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;
import java.util.*;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PolymorphicBase implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private PolymorphicBase parent;
    private List<PolymorphicBase> list = new ArrayList<PolymorphicBase>();
    private Set<PolymorphicBase> children = new HashSet<PolymorphicBase>();
    private Map<String, PolymorphicBase> map = new HashMap<String, PolymorphicBase>();
    private PolymorphicBaseContainer container;

    public PolymorphicBase() {
    }

    @Id
    @GeneratedValue
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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    public PolymorphicBase getParent() {
        return parent;
    }

    public void setParent(PolymorphicBase parent) {
        this.parent = parent;
    }

    @OneToMany
    @OrderColumn(name = "list_idx", nullable = false)
    @JoinTable(name = "polymorphic_list")
    public List<PolymorphicBase> getList() {
        return list;
    }

    public void setList(List<PolymorphicBase> list) {
        this.list = list;
    }

    @OneToMany(mappedBy = "parent")
    public Set<PolymorphicBase> getChildren() {
        return children;
    }

    public void setChildren(Set<PolymorphicBase> children) {
        this.children = children;
    }

    @OneToMany
    @JoinTable(name = "polymorphic_map")
    @MapKeyColumn(length = 20, nullable = false)
    public Map<String, PolymorphicBase> getMap() {
        return map;
    }

    public void setMap(Map<String, PolymorphicBase> map) {
        this.map = map;
    }

    @OneToOne(mappedBy = "owner")
    public PolymorphicBaseContainer getContainer() {
        return container;
    }

    public void setContainer(PolymorphicBaseContainer container) {
        this.container = container;
    }
}
