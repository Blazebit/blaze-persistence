
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
