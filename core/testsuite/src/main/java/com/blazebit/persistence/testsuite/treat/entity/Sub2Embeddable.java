
/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.treat.entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Sub2Embeddable<T extends Base<T, ?>> {
    
    public Integer getSub2SomeValue();
    
    public void setSub2SomeValue(Integer sub2SomeValue);
    
    public T getSub2Parent();

    public void setSub2Parent(T sub2Parent);

    public List<? extends T> getSub2List();

    public void setSub2List(List<? extends T> sub2List);

    public Set<? extends T> getSub2Children();

    public void setSub2Children(Set<? extends T> sub2Children);

    public Map<? extends T, ? extends T> getSub2Map();

    public void setSub2Map(Map<? extends T, ? extends T> sub2Map);
    
}
