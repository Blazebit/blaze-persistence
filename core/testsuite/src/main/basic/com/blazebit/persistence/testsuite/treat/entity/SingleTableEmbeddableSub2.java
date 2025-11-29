/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.treat.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Transient;

@Embeddable
public class SingleTableEmbeddableSub2 implements Sub2Embeddable<SingleTableBase>, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer sub2SomeValue;
    private SingleTableBase sub2Parent;
    private List<SingleTableBase> sub2List = new ArrayList<>();
    private Set<SingleTableSub2> sub2Children = new HashSet<>();
    private Map<SingleTableBase, SingleTableBase> sub2Map = new HashMap<>();

    public SingleTableEmbeddableSub2() {
    }

    public SingleTableEmbeddableSub2(SingleTableBase sub2Parent) {
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
    @JoinColumn(name = "embeddableSub2Parent")
    public SingleTableBase getSub2Parent() {
        return sub2Parent;
    }

    @Override
    public void setSub2Parent(SingleTableBase sub2Parent) {
        this.sub2Parent = sub2Parent;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    @JoinTable(name = "stes2_list")
    public List<SingleTableBase> getSub2List() {
        return sub2List;
    }

    @Override
    public void setSub2List(List<? extends SingleTableBase> sub2List) {
        this.sub2List = (List<SingleTableBase>) sub2List;
    }

    @Override
    @OneToMany
//    @JoinTable(name = "stes2_children")
    @JoinColumn(name = "embeddableSub2Parent")
    public Set<SingleTableSub2> getSub2Children() {
        return sub2Children;
    }

    @Override
    public void setSub2Children(Set<? extends SingleTableBase> sub2Children) {
        this.sub2Children = (Set<SingleTableSub2>) sub2Children;
    }

    // Apparently EclipseLink does not support mapping a map in an embeddable
    @Override
    @Transient
//    @ManyToMany
//    @JoinTable(name = "stes2_map")
//    @MapKeyColumn(name = "stes2_map_key", nullable = false, length = 20)
    public Map<SingleTableBase, SingleTableBase> getSub2Map() {
        return sub2Map;
    }

    @Override
    public void setSub2Map(Map<? extends SingleTableBase, ? extends SingleTableBase> sub2Map) {
        this.sub2Map = (Map<SingleTableBase, SingleTableBase>) sub2Map;
    }
}
