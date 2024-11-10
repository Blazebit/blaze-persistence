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
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;

@Embeddable
public class JoinedEmbeddable implements BaseEmbeddable<JoinedBase>, Serializable {
    private static final long serialVersionUID = 1L;

    private JoinedBase parent;
    private List<JoinedBase> list = new ArrayList<>();
    private Set<JoinedBase> children = new HashSet<>();
    private Map<JoinedBase, JoinedBase> map = new HashMap<>();

    public JoinedEmbeddable() {
    }

    public JoinedEmbeddable(JoinedBase parent) {
        this.parent = parent;
    }

    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embeddableParent")
    public JoinedBase getParent() {
        return parent;
    }

    @Override
    public void setParent(JoinedBase parent) {
        this.parent = parent;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    @JoinTable(name = "je_list")
    public List<JoinedBase> getList() {
        return list;
    }

    @Override
    public void setList(List<? extends JoinedBase> list) {
        this.list = (List<JoinedBase>) list;
    }

    @Override
    @OneToMany
    @JoinColumn(name = "embeddableParent")
    public Set<JoinedBase> getChildren() {
        return children;
    }

    @Override
    public void setChildren(Set<? extends JoinedBase> children) {
        this.children = (Set<JoinedBase>) children;
    }

    @Override
    @ManyToMany
    @JoinTable(name = "je_map")
    @MapKeyColumn(name = "je_map_key", nullable = false, length = 20)
    public Map<JoinedBase, JoinedBase> getMap() {
        return map;
    }

    @Override
    public void setMap(Map<? extends JoinedBase, ? extends JoinedBase> map) {
        this.map = (Map<JoinedBase, JoinedBase>) map;
    }
}
