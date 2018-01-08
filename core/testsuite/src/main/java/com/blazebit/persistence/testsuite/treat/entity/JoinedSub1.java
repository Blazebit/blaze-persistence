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

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Table(name = "joined_sub_1")
public class JoinedSub1 extends JoinedBase implements Sub1<JoinedBase, JoinedEmbeddable, JoinedEmbeddableSub1> {
    private static final long serialVersionUID = 1L;

    private IntIdEntity relation1;
    private JoinedBase parent1;
    private Integer sub1Value;
    private IntValueEmbeddable sub1Embeddable = new IntValueEmbeddable();
    private JoinedEmbeddableSub1 embeddable1 = new JoinedEmbeddableSub1();
    private List<JoinedBase> list1 = new ArrayList<>();
    private Set<JoinedSub1> children1 = new HashSet<>();
    private Map<JoinedBase, JoinedBase> map1 = new HashMap<>();

    public JoinedSub1() {
    }

    public JoinedSub1(String name) {
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
    public JoinedBase getParent1() {
        return parent1;
    }

    @Override
    public void setParent1(JoinedBase parent1) {
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
    public JoinedEmbeddableSub1 getEmbeddable1() {
        return embeddable1;
    }

    @Override
    public void setEmbeddable1(JoinedEmbeddableSub1 embeddable1) {
        this.embeddable1 = embeddable1;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    @JoinTable(name = "js1_list1")
    public List<JoinedBase> getList1() {
        return list1;
    }

    @Override
    public void setList1(List<? extends JoinedBase> list1) {
        this.list1 = (List<JoinedBase>) list1;
    }

    @Override
    @OneToMany(mappedBy = "parent1")
    public Set<JoinedSub1> getChildren1() {
        return children1;
    }

    @Override
    public void setChildren1(Set<? extends JoinedBase> children1) {
        this.children1 = (Set<JoinedSub1>) children1;
    }

    @Override
    @ManyToMany
    @JoinTable(name = "js1_map1")
    @MapKeyColumn(name = "js1_map_key", nullable = false, length = 20)
    public Map<JoinedBase, JoinedBase> getMap1() {
        return map1;
    }

    @Override
    public void setMap1(Map<? extends JoinedBase, ? extends JoinedBase> map1) {
        this.map1 = (Map<JoinedBase, JoinedBase>) map1;
    }
    
}
