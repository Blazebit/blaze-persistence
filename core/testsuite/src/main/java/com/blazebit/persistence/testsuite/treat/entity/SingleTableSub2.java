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
public class SingleTableSub2 extends SingleTableBase implements Sub2<SingleTableBase, SingleTableEmbeddable, SingleTableEmbeddableSub2> {
    private static final long serialVersionUID = 1L;

    private IntIdEntity relation2;
    private SingleTableBase parent2;
    private Integer sub2Value;
    private IntValueEmbeddable sub2Embeddable = new IntValueEmbeddable();
    private SingleTableEmbeddableSub2 embeddable2 = new SingleTableEmbeddableSub2();
    private List<SingleTableBase> list2 = new ArrayList<>();
    private Set<SingleTableSub2> children2 = new HashSet<>();
    private Map<SingleTableBase, SingleTableBase> map2 = new HashMap<>();

    public SingleTableSub2() {
    }

    public SingleTableSub2(String name) {
        super(name);
    }

    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    public IntIdEntity getRelation2() {
        return relation2;
    }

    @Override
    public void setRelation2(IntIdEntity relation2) {
        this.relation2 = relation2;
    }
    
    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    public SingleTableBase getParent2() {
        return parent2;
    }

    @Override
    public void setParent2(SingleTableBase parent2) {
        this.parent2 = parent2;
    }

    @Override
    public Integer getSub2Value() {
        return sub2Value;
    }

    @Override
    public void setSub2Value(Integer sub2Value) {
        this.sub2Value = sub2Value;
    }

    @Override
    @Embedded
    @AttributeOverride(name = "someValue", column = @Column(name = "someValue1"))
    public IntValueEmbeddable getSub2Embeddable() {
        return sub2Embeddable;
    }

    @Override
    public void setSub2Embeddable(IntValueEmbeddable sub2Embeddable) {
        this.sub2Embeddable = sub2Embeddable;
    }

    @Override
    @Embedded
    public SingleTableEmbeddableSub2 getEmbeddable2() {
        return embeddable2;
    }

    @Override
    public void setEmbeddable2(SingleTableEmbeddableSub2 embeddable2) {
        this.embeddable2 = embeddable2;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    @JoinTable(name = "sts2_list2")
    public List<SingleTableBase> getList2() {
        return list2;
    }

    @Override
    public void setList2(List<? extends SingleTableBase> list2) {
        this.list2 = (List<SingleTableBase>) list2;
    }

    @Override
    @OneToMany(mappedBy = "parent2")
    public Set<SingleTableSub2> getChildren2() {
        return children2;
    }

    @Override
    public void setChildren2(Set<? extends SingleTableBase> children2) {
        this.children2 = (Set<SingleTableSub2>) children2;
    }

    @Override
    @ManyToMany
    @JoinTable(name = "sts2_map2")
    @MapKeyColumn(name = "sts2_map2_key", nullable = false, length = 20)
    public Map<SingleTableBase, SingleTableBase> getMap2() {
        return map2;
    }

    @Override
    public void setMap2(Map<? extends SingleTableBase, ? extends SingleTableBase> map2) {
        this.map2 = (Map<SingleTableBase, SingleTableBase>) map2;
    }
}
