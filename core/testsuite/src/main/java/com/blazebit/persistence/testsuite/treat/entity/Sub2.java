
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

public interface Sub2<T extends Base<T, B>, B extends BaseEmbeddable<T>, B2 extends Sub2Embeddable<T>> extends Base<T, B> {
    
    public IntIdEntity getRelation2();

    public void setRelation2(IntIdEntity relation1);

    public T getParent2();

    public void setParent2(T parent2);

    public B2 getEmbeddable2();

    public void setEmbeddable2(B2 embeddable2);

    public Integer getSub2Value();

    public void setSub2Value(Integer sub2Value);

    public IntValueEmbeddable getSub2Embeddable();

    public void setSub2Embeddable(IntValueEmbeddable sub2Embeddable);

    public List<? extends T> getList2();

    public void setList2(List<? extends T> list2);

    public Set<? extends T> getChildren2();

    public void setChildren2(Set<? extends T> children2);

    public Map<? extends T, ? extends T> getMap2();

    public void setMap2(Map<? extends T, ? extends T> map2);
    
}
