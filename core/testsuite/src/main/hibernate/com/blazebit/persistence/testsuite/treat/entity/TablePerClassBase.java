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

import org.hibernate.annotations.ForeignKey;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Table(name = "table_per_class_base")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class TablePerClassBase implements Serializable, Base<TablePerClassBase, TablePerClassEmbeddable> {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Integer value;
    private TablePerClassBase parent;
    private TablePerClassEmbeddable embeddable = new TablePerClassEmbeddable();
    private List<TablePerClassBase> list = new ArrayList<>();
    private Map<TablePerClassBase, TablePerClassBase> map = new HashMap<>();
    private Set<TablePerClassBase> children = new HashSet<>();

    public TablePerClassBase() {
    }

    public TablePerClassBase(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Id
    @Override
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
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @ForeignKey(name = "none")
    public TablePerClassBase getParent() {
        return parent;
    }

    @Override
    public void setParent(TablePerClassBase parent) {
        this.parent = parent;
    }

    @Override
    @Embedded
    public TablePerClassEmbeddable getEmbeddable() {
        return embeddable;
    }

    @Override
    public void setEmbeddable(TablePerClassEmbeddable embeddable) {
        this.embeddable = embeddable;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinTable(name = "tpcb_list")
    @ForeignKey(name = "none", inverseName = "none")
    public List<TablePerClassBase> getList() {
        return list;
    }

    @Override
    public void setList(List<? extends TablePerClassBase> list) {
        this.list = (List<TablePerClassBase>) list;
    }

    @Override
    @ManyToMany
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinTable(name = "tpcb_map")
    @ForeignKey(name = "none", inverseName = "none")
    @MapKeyColumn(name = "tpcb_map_key", nullable = false, length = 20)
    public Map<TablePerClassBase, TablePerClassBase> getMap() {
        return map;
    }

    @Override
    public void setMap(Map<? extends TablePerClassBase, ? extends TablePerClassBase> map) {
        this.map = (Map<TablePerClassBase, TablePerClassBase>) map;
    }

    @Override
    @OneToMany(mappedBy = "parent")
    public Set<TablePerClassBase> getChildren() {
        return children;
    }

    @Override
    public void setChildren(Set<? extends TablePerClassBase> children) {
        this.children = (Set<TablePerClassBase>) children;
    }
}
