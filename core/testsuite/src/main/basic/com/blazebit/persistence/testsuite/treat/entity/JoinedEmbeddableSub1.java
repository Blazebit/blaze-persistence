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
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;

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
