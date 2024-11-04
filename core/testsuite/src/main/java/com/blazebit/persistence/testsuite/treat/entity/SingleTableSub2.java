/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.treat.entity;

import com.blazebit.persistence.testsuite.entity.IntIdEntity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
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
