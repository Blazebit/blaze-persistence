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

package com.blazebit.persistence.testsuite.treat.entity;

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
