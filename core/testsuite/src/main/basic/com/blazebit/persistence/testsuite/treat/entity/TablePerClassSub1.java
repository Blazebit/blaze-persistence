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

import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "table_per_class_sub_1")
@AssociationOverrides({
    @AssociationOverride(
            name = "embeddable.list",
            joinTable = @JoinTable(name = "tpces1_list", inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    ),
    @AssociationOverride(
            name = "embeddable.map",
            joinTable = @JoinTable(name = "tpces1_map", inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    )
})
public class TablePerClassSub1 extends TablePerClassBase implements Sub1<TablePerClassBase, TablePerClassEmbeddable, TablePerClassEmbeddableSub1> {
    private static final long serialVersionUID = 1L;

    private IntIdEntity relation1;
    private TablePerClassBase parent1;
    private Integer sub1Value;
    private IntValueEmbeddable sub1Embeddable = new IntValueEmbeddable();
    private List<TablePerClassBase> list = new ArrayList<>();
    private Map<TablePerClassBase, TablePerClassBase> map = new HashMap<>();
    private TablePerClassEmbeddableSub1 embeddable1 = new TablePerClassEmbeddableSub1();
    private List<TablePerClassBase> list1 = new ArrayList<>();
    private Set<TablePerClassBase> children1 = new HashSet<>();
    private Map<TablePerClassBase, TablePerClassBase> map1 = new HashMap<>();

    public TablePerClassSub1() {
    }

    public TablePerClassSub1(Long id, String name) {
        super(id, name);
    }

    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    public IntIdEntity getRelation1() {
        return relation1;
    }

    @Override
    public void setRelation1(IntIdEntity relation1) {
        this.relation1 = relation1;
    }

    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public TablePerClassBase getParent1() {
        return parent1;
    }

    @Override
    public void setParent1(TablePerClassBase parent1) {
        this.parent1 = parent1;
    }

    @Override
    public Integer getSub1Value() {
        return sub1Value;
    }

    @Override
    public void setSub1Value(Integer sub1Value) {
        this.sub1Value = sub1Value;
    }

    @Override
    @Embedded
    public IntValueEmbeddable getSub1Embeddable() {
        return sub1Embeddable;
    }

    @Override
    public void setSub1Embeddable(IntValueEmbeddable sub1Embeddable) {
        this.sub1Embeddable = sub1Embeddable;
    }

    @Override
    @Embedded
    public TablePerClassEmbeddableSub1 getEmbeddable1() {
        return embeddable1;
    }

    @Override
    public void setEmbeddable1(TablePerClassEmbeddableSub1 embeddable1) {
        this.embeddable1 = embeddable1;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinTable(name = "tpcs1_list", inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
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
    @JoinTable(name = "tpcs1_map", inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @MapKeyColumn(name = "tpcs1_map_key", nullable = false, length = 20)
    public Map<TablePerClassBase, TablePerClassBase> getMap() {
        return map;
    }

    @Override
    public void setMap(Map<? extends TablePerClassBase, ? extends TablePerClassBase> map) {
        this.map = (Map<TablePerClassBase, TablePerClassBase>) map;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinTable(name = "tpcs1_list1", inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public List<TablePerClassBase> getList1() {
        return list1;
    }

    @Override
    public void setList1(List<? extends TablePerClassBase> list1) {
        this.list1 = (List<TablePerClassBase>) list1;
    }

    @Override
    @OneToMany(mappedBy = "parent1", targetEntity = TablePerClassSub1.class)
    public Set<TablePerClassBase> getChildren1() {
        return children1;
    }

    @Override
    public void setChildren1(Set<? extends TablePerClassBase> children1) {
        this.children1 = (Set<TablePerClassBase>) children1;
    }
    
    @Override
    @ManyToMany
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinTable(name = "tpcs1_map1", inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @MapKeyColumn(name = "tpcs1_map1_key", nullable = false, length = 20)
    public Map<TablePerClassBase, TablePerClassBase> getMap1() {
        return map1;
    }

    @Override
    public void setMap1(Map<? extends TablePerClassBase, ? extends TablePerClassBase> map1) {
        this.map1 = (Map<TablePerClassBase, TablePerClassBase>) map1;
    }
}
