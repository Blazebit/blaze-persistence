
/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.treat.entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Sub1Embeddable<T extends Base<T, ?>> {
    
    public Integer getSub1SomeValue();
    
    public void setSub1SomeValue(Integer sub1SomeValue);
    
    public T getSub1Parent();

    public void setSub1Parent(T sub1Parent);

    public List<? extends T> getSub1List();

    public void setSub1List(List<? extends T> sub1List);

    public Set<? extends T> getSub1Children();

    public void setSub1Children(Set<? extends T> sub1Children);

    public Map<? extends T, ? extends T> getSub1Map();

    public void setSub1Map(Map<? extends T, ? extends T> sub1Map);
    
}
