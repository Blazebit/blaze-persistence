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
public class JoinedEmbeddableSub1 implements Sub1Embeddable<JoinedBase>, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer sub1SomeValue;
    private JoinedBase sub1Parent;
    private List<JoinedBase> sub1List = new ArrayList<>();
    private Set<JoinedSub1> sub1Children = new HashSet<>();
    private Map<JoinedBase, JoinedBase> sub1Map = new HashMap<>();

    public JoinedEmbeddableSub1() {
    }

    public JoinedEmbeddableSub1(JoinedBase sub1Parent) {
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
    @JoinColumn(name = "embeddableSub1Parent")
    public JoinedBase getSub1Parent() {
        return sub1Parent;
    }

    @Override
    public void setSub1Parent(JoinedBase sub1Parent) {
        this.sub1Parent = sub1Parent;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    @JoinTable(name = "jes1_list")
    public List<JoinedBase> getSub1List() {
        return sub1List;
    }

    @Override
    public void setSub1List(List<? extends JoinedBase> sub1List) {
        this.sub1List = (List<JoinedBase>) sub1List;
    }

    @Override
    @OneToMany
//    @JoinTable(name = "jes1_children")
    @JoinColumn(name = "embeddableSub1Parent")
    public Set<JoinedSub1> getSub1Children() {
        return sub1Children;
    }

    @Override
    public void setSub1Children(Set<? extends JoinedBase> sub1Children) {
        this.sub1Children = (Set<JoinedSub1>) sub1Children;
    }

    // Apparently EclipseLink does not support mapping a map in an embeddable
    @Override
    @Transient
//    @ManyToMany
//    @JoinTable(name = "jes1_map")
//    @MapKeyColumn(name = "jes1_map_key", nullable = false, length = 20)
    public Map<JoinedBase, JoinedBase> getSub1Map() {
        return sub1Map;
    }

    @Override
    public void setSub1Map(Map<? extends JoinedBase, ? extends JoinedBase> sub1Map) {
        this.sub1Map = (Map<JoinedBase, JoinedBase>) sub1Map;
    }

}
