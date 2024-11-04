/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.treat.entity;

import com.blazebit.persistence.testsuite.entity.IntIdEntity;

import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "table_per_class_sub_2")
public class TablePerClassSub2 extends TablePerClassBase implements Sub2<TablePerClassBase, TablePerClassEmbeddable, TablePerClassEmbeddableSub2> {
    private static final long serialVersionUID = 1L;

    private IntIdEntity relation2;
    private TablePerClassBase parent2;
    private Integer sub2Value;
    private IntValueEmbeddable sub2Embeddable = new IntValueEmbeddable();
    private TablePerClassEmbeddableSub2 embeddable2 = new TablePerClassEmbeddableSub2();
    private List<TablePerClassBase> list2 = new ArrayList<>();
    private Set<TablePerClassBase> children2 = new HashSet<>();
    private Map<TablePerClassBase, TablePerClassBase> map2 = new HashMap<>();

    public TablePerClassSub2() {
    }

    public TablePerClassSub2(Long id, String name) {
        super(id, name);
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
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public TablePerClassBase getParent2() {
        return parent2;
    }

    @Override
    public void setParent2(TablePerClassBase parent2) {
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
    public IntValueEmbeddable getSub2Embeddable() {
        return sub2Embeddable;
    }

    @Override
    public void setSub2Embeddable(IntValueEmbeddable sub2Embeddable) {
        this.sub2Embeddable = sub2Embeddable;
    }

    @Override
    @Embedded
    public TablePerClassEmbeddableSub2 getEmbeddable2() {
        return embeddable2;
    }

    @Override
    public void setEmbeddable2(TablePerClassEmbeddableSub2 embeddable2) {
        this.embeddable2 = embeddable2;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinTable(name = "tpcs2_list2", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public List<TablePerClassBase> getList2() {
        return list2;
    }

    @Override
    public void setList2(List<? extends TablePerClassBase> list2) {
        this.list2 = (List<TablePerClassBase>) list2;
    }

    @Override
    @OneToMany(mappedBy = "parent2", targetEntity = TablePerClassSub2.class)
    public Set<TablePerClassBase> getChildren2() {
        return children2;
    }

    @Override
    public void setChildren2(Set<? extends TablePerClassBase> children2) {
        this.children2 = (Set<TablePerClassBase>) children2;
    }
    
    @Override
    @ManyToMany
    // We can't have a constraint in this case because we don't know the exact table this will refer to
    @JoinTable(name = "tpcs2_map2", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @MapKeyColumn(name = "tpcs2_map2_key", nullable = false, length = 20)
    public Map<TablePerClassBase, TablePerClassBase> getMap2() {
        return map2;
    }

    @Override
    public void setMap2(Map<? extends TablePerClassBase, ? extends TablePerClassBase> map2) {
        this.map2 = (Map<TablePerClassBase, TablePerClassBase>) map2;
    }
}
