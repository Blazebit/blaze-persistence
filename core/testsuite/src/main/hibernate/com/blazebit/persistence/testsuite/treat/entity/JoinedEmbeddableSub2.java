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
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

@Embeddable
public class JoinedEmbeddableSub2 implements Sub2Embeddable<JoinedBase>, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer sub2SomeValue;
    private JoinedBase sub2Parent;
    private List<JoinedBase> sub2List = new ArrayList<>();
    private Set<JoinedSub2> sub2Children = new HashSet<>();
    private Map<JoinedBase, JoinedBase> sub2Map = new HashMap<>();

    public JoinedEmbeddableSub2() {
    }

    public JoinedEmbeddableSub2(JoinedBase sub2Parent) {
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
    public JoinedBase getSub2Parent() {
        return sub2Parent;
    }

    @Override
    public void setSub2Parent(JoinedBase sub2Parent) {
        this.sub2Parent = sub2Parent;
    }

    @Override
    @ManyToMany
    @OrderColumn(name = "list_idx", nullable = false)
    @JoinTable(name = "jes2_list")
    public List<JoinedBase> getSub2List() {
        return sub2List;
    }

    @Override
    public void setSub2List(List<? extends JoinedBase> sub2List) {
        this.sub2List = (List<JoinedBase>) sub2List;
    }

    @Override
    @OneToMany
    @JoinColumn(name = "embeddableSub2Parent")
    public Set<JoinedSub2> getSub2Children() {
        return sub2Children;
    }

    @Override
    public void setSub2Children(Set<? extends JoinedBase> sub2Children) {
        this.sub2Children = (Set<JoinedSub2>) sub2Children;
    }

    @Override
    @ManyToMany
    @JoinTable(name = "jes2_map")
    @MapKeyColumn(name = "jes2_map_key", nullable = false, length = 20)
    public Map<JoinedBase, JoinedBase> getSub2Map() {
        return sub2Map;
    }

    @Override
    public void setSub2Map(Map<? extends JoinedBase, ? extends JoinedBase> sub2Map) {
        this.sub2Map = (Map<JoinedBase, JoinedBase>) sub2Map;
    }
}
