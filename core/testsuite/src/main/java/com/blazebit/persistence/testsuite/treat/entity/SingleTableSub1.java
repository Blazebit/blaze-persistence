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

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
public class SingleTableSub1 extends SingleTableBase implements Sub1<SingleTableBase, SingleTableEmbeddable, SingleTableEmbeddableSub1> {
    private static final long serialVersionUID = 1L;

    private IntIdEntity relation1;
    private SingleTableBase parent1;
    private Integer sub1Value;
    private IntValueEmbeddable sub1Embeddable = new IntValueEmbeddable();
    private SingleTableEmbeddableSub1 embeddable1 = new SingleTableEmbeddableSub1();
    private List<SingleTableBase> list1 = new ArrayList<>();
    private Set<SingleTableSub1> children1 = new HashSet<>();
    private Map<SingleTableBase, SingleTableBase> map1 = new HashMap<>();

    public SingleTableSub1() {
    }

    public SingleTableSub1(String name) {
        super(name);
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
    public SingleTableBase getParent1() {
        return parent1;
    }

    @Override
    public void setParent1(SingleTableBase parent1) {
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
    @AttributeOverride(name = "someValue", column = @Column(name = "someValue1"))
    public IntValueEmbeddable getSub1Embeddable() {
        return sub1Embeddable;
    }

    @Override
    public void setSub1Embeddable(IntValueEmbeddable sub1Embeddable) {
        this.sub1Embeddable = sub1Embeddable;
    }

    @Override
    @Embedded
    public SingleTableEmbeddableSub1 getEmbeddable1() {
        return embeddable1;
    }

    @Override
    public void setEmbeddable1(SingleTableEmbeddableSub1 embeddable1) {
        this.embeddable1 = embeddable1;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    @JoinTable(name = "sts1_list1")
    public List<SingleTableBase> getList1() {
        return list1;
    }

    @Override
    public void setList1(List<? extends SingleTableBase> list1) {
        this.list1 = (List<SingleTableBase>) list1;
    }

    @Override
    @OneToMany(mappedBy = "parent1")
    public Set<SingleTableSub1> getChildren1() {
        return children1;
    }

    @Override
    public void setChildren1(Set<? extends SingleTableBase> children1) {
        this.children1 = (Set<SingleTableSub1>) children1;
    }

    @Override
    @ManyToMany
    @JoinTable(name = "sts1_map1")
    @MapKeyColumn(name = "sts1_map1_key", nullable = false, length = 20)
    public Map<SingleTableBase, SingleTableBase> getMap1() {
        return map1;
    }

    @Override
    public void setMap1(Map<? extends SingleTableBase, ? extends SingleTableBase> map1) {
        this.map1 = (Map<SingleTableBase, SingleTableBase>) map1;
    }
}
