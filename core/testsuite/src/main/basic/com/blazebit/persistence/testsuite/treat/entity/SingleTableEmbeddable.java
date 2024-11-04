/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.treat.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Transient;

@Embeddable
public class SingleTableEmbeddable implements BaseEmbeddable<SingleTableBase>, Serializable {
    private static final long serialVersionUID = 1L;

    private SingleTableBase parent;
    private List<SingleTableBase> list = new ArrayList<>();
    private Set<SingleTableBase> children = new HashSet<>();
    private Map<SingleTableBase, SingleTableBase> map = new HashMap<>();

    public SingleTableEmbeddable() {
    }

    public SingleTableEmbeddable(SingleTableBase parent) {
        this.parent = parent;
    }

    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embeddableParent")
    public SingleTableBase getParent() {
        return parent;
    }

    @Override
    public void setParent(SingleTableBase parent) {
        this.parent = parent;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    @JoinTable(name = "ste_list")
    public List<SingleTableBase> getList() {
        return list;
    }

    @Override
    public void setList(List<? extends SingleTableBase> list) {
        this.list = (List<SingleTableBase>) list;
    }

    @Override
    @OneToMany
//    @JoinTable(name = "ste_children")
    @JoinColumn(name = "embeddableParent")
    public Set<SingleTableBase> getChildren() {
        return children;
    }

    @Override
    public void setChildren(Set<? extends SingleTableBase> children) {
        this.children = (Set<SingleTableBase>) children;
    }

    // Apparently EclipseLink does not support mapping a map in an embeddable
    @Override
    @Transient
//    @ManyToMany
//    @JoinTable(name = "ste_map")
//    @MapKeyColumn(name = "ste_map_key", nullable = false, length = 20)
    public Map<SingleTableBase, SingleTableBase> getMap() {
        return map;
    }

    @Override
    public void setMap(Map<? extends SingleTableBase, ? extends SingleTableBase> map) {
        this.map = (Map<SingleTableBase, SingleTableBase>) map;
    }
}
