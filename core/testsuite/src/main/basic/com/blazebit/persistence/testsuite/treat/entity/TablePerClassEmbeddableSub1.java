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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.ConstraintMode;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;

@Embeddable
public class TablePerClassEmbeddableSub1 implements Sub1Embeddable<TablePerClassBase>, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer sub1SomeValue;
    private TablePerClassBase sub1Parent;
    private Set<TablePerClassSub1> sub1Children = new HashSet<>();
    private List<TablePerClassBase> sub1List = new ArrayList<>();
    private Map<TablePerClassBase, TablePerClassBase> sub1Map = new HashMap<>();

    public TablePerClassEmbeddableSub1() {
    }

    public TablePerClassEmbeddableSub1(TablePerClassBase sub1Parent) {
        this.sub1Parent = sub1Parent;
    }

    @Override
    public Integer getSub1SomeValue() {
        return sub1SomeValue;
    }

    @Override
    public void setSub1SomeValue(Integer sub1SomeValue) {
        this.sub1SomeValue = sub1SomeValue;
    }

    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinColumn(name = "embeddableSub1Parent", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public TablePerClassBase getSub1Parent() {
        return sub1Parent;
    }

    @Override
    public void setSub1Parent(TablePerClassBase sub1Parent) {
        this.sub1Parent = sub1Parent;
    }

    @Override
    @OneToMany
    @JoinColumn(name = "embeddableSub1Parent")
    public Set<TablePerClassSub1> getSub1Children() {
        return sub1Children;
    }

    @Override
    public void setSub1Children(Set<? extends TablePerClassBase> sub1Children) {
        this.sub1Children = (Set<TablePerClassSub1>) sub1Children;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinTable(name = "tpces1_list", inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public List<TablePerClassBase> getSub1List() {
        return sub1List;
    }

    @Override
    public void setSub1List(List<? extends TablePerClassBase> sub1List) {
        this.sub1List = (List<TablePerClassBase>) sub1List;
    }

    // Apparently EclipseLink does not support mapping a map in an embeddable
    @Override
    @Transient
//    @ManyToMany
//    // We can't have a constraint in this case because we don't know the exact table this will refer to
//    @JoinTable(name = "tpces1_map", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
//    @MapKeyColumn(name = "tpces1_map_key", nullable = false, length = 20)
    public Map<TablePerClassBase, TablePerClassBase> getSub1Map() {
        return sub1Map;
    }

    @Override
    public void setSub1Map(Map<? extends TablePerClassBase, ? extends TablePerClassBase> sub1Map) {
        this.sub1Map = (Map<TablePerClassBase, TablePerClassBase>) sub1Map;
    }
}
