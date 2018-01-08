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
public class TablePerClassEmbeddableSub2 implements Sub2Embeddable<TablePerClassBase>, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer sub2SomeValue;
    private TablePerClassBase sub2Parent;
    private Set<TablePerClassSub2> sub2Children = new HashSet<>();
    private List<TablePerClassBase> sub2List = new ArrayList<>();
    private Map<TablePerClassBase, TablePerClassBase> sub2Map = new HashMap<>();

    public TablePerClassEmbeddableSub2() {
    }

    public TablePerClassEmbeddableSub2(TablePerClassBase sub2Parent) {
        this.sub2Parent = sub2Parent;
    }

    @Override
    public Integer getSub2SomeValue() {
        return sub2SomeValue;
    }

    @Override
    public void setSub2SomeValue(Integer sub2SomeValue) {
        this.sub2SomeValue = sub2SomeValue;
    }

    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinColumn(name = "embeddableSub2Parent")
    @ForeignKey(name = "none")
    public TablePerClassBase getSub2Parent() {
        return sub2Parent;
    }

    @Override
    public void setSub2Parent(TablePerClassBase sub2Parent) {
        this.sub2Parent = sub2Parent;
    }

    @Override
    @OneToMany
    @JoinColumn(name = "embeddableSub2Parent")
    public Set<TablePerClassSub2> getSub2Children() {
        return sub2Children;
    }

    @Override
    public void setSub2Children(Set<? extends TablePerClassBase> sub2Children) {
        this.sub2Children = (Set<TablePerClassSub2>) sub2Children;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinTable(name = "tpces2_list")
    @ForeignKey(name = "none", inverseName = "none")
    public List<TablePerClassBase> getSub2List() {
        return sub2List;
    }

    @Override
    public void setSub2List(List<? extends TablePerClassBase> sub2List) {
        this.sub2List = (List<TablePerClassBase>) sub2List;
    }

    @Override
    @ManyToMany
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinTable(name = "tpces2_map")
    @ForeignKey(name = "none", inverseName = "none")
    @MapKeyColumn(name = "tpces2_map_key", nullable = false, length = 20)
    public Map<TablePerClassBase, TablePerClassBase> getSub2Map() {
        return sub2Map;
    }

    @Override
    public void setSub2Map(Map<? extends TablePerClassBase, ? extends TablePerClassBase> sub2Map) {
        this.sub2Map = (Map<TablePerClassBase, TablePerClassBase>) sub2Map;
    }
}
