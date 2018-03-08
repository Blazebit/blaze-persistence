/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;
import java.util.*;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

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
}
