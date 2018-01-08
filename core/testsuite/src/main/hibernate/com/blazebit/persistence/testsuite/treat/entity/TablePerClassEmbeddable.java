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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

@Embeddable
public class TablePerClassEmbeddable implements BaseEmbeddable<TablePerClassBase>, Serializable {
    private static final long serialVersionUID = 1L;

    private TablePerClassBase parent;
    private Set<TablePerClassBase> children = new HashSet<>();
    private List<TablePerClassBase> list = new ArrayList<>();
    private Map<TablePerClassBase, TablePerClassBase> map = new HashMap<>();

    public TablePerClassEmbeddable() {
    }

    public TablePerClassEmbeddable(TablePerClassBase parent) {
        this.parent = parent;
    }

    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinColumn(name = "embeddableParent")
    @ForeignKey(name = "none")
    public TablePerClassBase getParent() {
        return parent;
    }

    @Override
    public void setParent(TablePerClassBase parent) {
        this.parent = parent;
    }

    @Override
    @OneToMany
    @JoinColumn(name = "embeddableParent")
    public Set<TablePerClassBase> getChildren() {
        return children;
    }

    @Override
    public void setChildren(Set<? extends TablePerClassBase> children) {
        this.children = (Set<TablePerClassBase>) children;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinTable(name = "tpce_list")
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
    @JoinTable(name = "tpce_map")
    @ForeignKey(name = "none", inverseName = "none")
    @MapKeyColumn(name = "tpce_map_key", nullable = false, length = 20)
    public Map<TablePerClassBase, TablePerClassBase> getMap() {
        return map;
    }

    @Override
    public void setMap(Map<? extends TablePerClassBase, ? extends TablePerClassBase> map) {
        this.map = (Map<TablePerClassBase, TablePerClassBase>) map;
    }
}
