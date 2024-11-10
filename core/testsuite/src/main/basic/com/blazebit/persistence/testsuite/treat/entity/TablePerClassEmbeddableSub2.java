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
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Transient;

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
    @JoinColumn(name = "embeddableSub2Parent", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
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
    @JoinTable(name = "tpces2_list", inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public List<TablePerClassBase> getSub2List() {
        return sub2List;
    }

    @Override
    public void setSub2List(List<? extends TablePerClassBase> sub2List) {
        this.sub2List = (List<TablePerClassBase>) sub2List;
    }

    // Apparently EclipseLink does not support mapping a map in an embeddable
    @Override
    @Transient
//    @ManyToMany
//    // We can't have a constraint in this case because we don't know the exact table this will refer to
//    @JoinTable(name = "tpces2_map", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
//    @MapKeyColumn(name = "tpces2_map_key", nullable = false, length = 20)
    public Map<TablePerClassBase, TablePerClassBase> getSub2Map() {
        return sub2Map;
    }

    @Override
    public void setSub2Map(Map<? extends TablePerClassBase, ? extends TablePerClassBase> sub2Map) {
        this.sub2Map = (Map<TablePerClassBase, TablePerClassBase>) sub2Map;
    }
}
