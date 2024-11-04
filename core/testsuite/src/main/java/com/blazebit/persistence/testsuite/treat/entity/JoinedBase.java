/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.treat.entity;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "joined_base")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class JoinedBase implements Serializable, Base<JoinedBase, JoinedEmbeddable> {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Integer value;
    private JoinedBase parent;
    private JoinedEmbeddable embeddable = new JoinedEmbeddable();
    private List<JoinedBase> list = new ArrayList<>();
    private Set<JoinedBase> children = new HashSet<>();
    private Map<JoinedBase, JoinedBase> map = new HashMap<>();

    public JoinedBase() {
    }

    public JoinedBase(String name) {
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
    public JoinedBase getParent() {
        return parent;
    }

    @Override
    public void setParent(JoinedBase parent) {
        this.parent = parent;
    }

    @Override
    @Embedded
    public JoinedEmbeddable getEmbeddable() {
        return embeddable;
    }

    @Override
    public void setEmbeddable(JoinedEmbeddable embeddable) {
        this.embeddable = embeddable;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    @JoinTable(name = "jb_list")
    public List<JoinedBase> getList() {
        return list;
    }

    @Override
    public void setList(List<? extends JoinedBase> list) {
        this.list = (List<JoinedBase>) list;
    }

    @Override
    @OneToMany(mappedBy = "parent")
    public Set<JoinedBase> getChildren() {
        return children;
    }

    @Override
    public void setChildren(Set<? extends JoinedBase> children) {
        this.children = (Set<JoinedBase>) children;
    }

    @Override
    @ManyToMany
    @JoinTable(name = "jb_map")
    @MapKeyColumn(name = "jb_map_key", nullable = false, length = 20)
    public Map<JoinedBase, JoinedBase> getMap() {
        return map;
    }

    @Override
    public void setMap(Map<? extends JoinedBase, ? extends JoinedBase> map) {
        this.map = (Map<JoinedBase, JoinedBase>) map;
    }
}
