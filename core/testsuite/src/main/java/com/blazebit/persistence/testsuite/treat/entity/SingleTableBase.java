/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.treat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "single_table_base")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class SingleTableBase implements Serializable, Base<SingleTableBase, SingleTableEmbeddable> {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Integer value;
    private SingleTableBase parent;
    private SingleTableEmbeddable embeddable = new SingleTableEmbeddable();
    private List<SingleTableBase> list = new ArrayList<>();
    private Set<SingleTableBase> children = new HashSet<>();
    private Map<SingleTableBase, SingleTableBase> map = new HashMap<>();

    public SingleTableBase() {
    }

    public SingleTableBase(String name) {
        this.name = name;
    }

    @Id
    @Override
    @GeneratedValue
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
    public SingleTableBase getParent() {
        return parent;
    }

    @Override
    public void setParent(SingleTableBase parent) {
        this.parent = parent;
    }

    @Override
    @Embedded
    public SingleTableEmbeddable getEmbeddable() {
        return embeddable;
    }

    @Override
    public void setEmbeddable(SingleTableEmbeddable embeddable) {
        this.embeddable = embeddable;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    @JoinTable(name = "stb_list")
    public List<SingleTableBase> getList() {
        return list;
    }

    @Override
    public void setList(List<? extends SingleTableBase> list) {
        this.list = (List<SingleTableBase>) list;
    }

    @Override
    @OneToMany(mappedBy = "parent")
    public Set<SingleTableBase> getChildren() {
        return children;
    }

    @Override
    public void setChildren(Set<? extends SingleTableBase> children) {
        this.children = (Set<SingleTableBase>) children;
    }

    @Override
    @ManyToMany
    @JoinTable(name = "stb_map")
    @MapKeyColumn(name = "stb_map_key", nullable = false, length = 20)
    public Map<SingleTableBase, SingleTableBase> getMap() {
        return map;
    }

    @Override
    public void setMap(Map<? extends SingleTableBase, ? extends SingleTableBase> map) {
        this.map = (Map<SingleTableBase, SingleTableBase>) map;
    }
}
