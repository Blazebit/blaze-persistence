
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

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Sub1<T extends Base<T, B>, B extends BaseEmbeddable<T>, B1 extends Sub1Embeddable<T>> extends Base<T, B> {
    
    public IntIdEntity getRelation1();

    public void setRelation1(IntIdEntity relation1);

    public T getParent1();

    public void setParent1(T parent1);

    public B1 getEmbeddable1();

    public void setEmbeddable1(B1 embeddable1);

    public Integer getSub1Value();

    public void setSub1Value(Integer sub1Value);

    public IntValueEmbeddable getSub1Embeddable();

    public void setSub1Embeddable(IntValueEmbeddable sub1Embeddable);

    public List<? extends T> getList1();

    public void setList1(List<? extends T> list1);

    public Set<? extends T> getChildren1();

    public void setChildren1(Set<? extends T> children1);

    public Map<? extends T, ? extends T> getMap1();

    public void setMap1(Map<? extends T, ? extends T> map1);
}
